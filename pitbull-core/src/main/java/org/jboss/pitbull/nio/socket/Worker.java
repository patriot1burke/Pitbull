package org.jboss.pitbull.nio.socket;

import org.jboss.pitbull.logging.Logger;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class Worker implements Runnable
{
   protected Selector selector;
   protected volatile boolean shutdown;
   protected volatile boolean idle;
   protected CountDownLatch shutdownLatch = new CountDownLatch(1);
   protected Queue<SocketChannel> registrationQueue = new ArrayDeque<SocketChannel>(10);
   protected static final Logger logger = Logger.getLogger(Worker.class);
   protected EventHandlerFactory factory;
   protected static final AtomicInteger counter = new AtomicInteger();

   public Worker(EventHandlerFactory factory) throws IOException
   {
      this.selector = Selector.open();
      this.factory = factory;
   }

   public void register(SocketChannel channel) throws IOException
   {
      synchronized (registrationQueue)
      {
         if (idle)
         {
            executeRegistration(channel);
            registrationQueue.notify();
         }
         else
         {
            registrationQueue.add(channel);
            selector.wakeup();
         }
      }
   }

   protected void executeRegistration(SocketChannel channel) throws IOException
   {
      logger.debug("Registered channel.");
      channel.configureBlocking(false);
      SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
      key.attach(new ManagedChannel(channel, key, factory.create()));
   }

   protected void processRegistrations() throws IOException
   {
      for (; ; )
      {
         SocketChannel channel = null;
         synchronized (registrationQueue)
         {
            channel = registrationQueue.poll();
         }
         if (channel == null) break;
         executeRegistration(channel);
      }
   }

   protected void processReads()
   {
      Set<SelectionKey> keys = selector.selectedKeys();
      for (SelectionKey key : keys)
      {
         int readyOps = key.readyOps();
         if ((readyOps & SelectionKey.OP_READ) != 0 || readyOps == 0)
         {
            ManagedChannel channel = (ManagedChannel) key.attachment();
            channel.getHandler().handleRead(channel);
            if (!channel.getChannel().isOpen())
            {
               key.cancel();
            }
         }
      }
   }

   public void shutdown()
   {
      synchronized (registrationQueue)
      {
         shutdown = true;
         registrationQueue.notify();
      }
      selector.wakeup();
      try
      {
         shutdownLatch.await();
      }
      catch (InterruptedException e)
      {
         throw new RuntimeException(e);
      }
   }

   @Override
   public void run()
   {
      String oldName = Thread.currentThread().getName();
      Thread.currentThread().setName("Pitbull Worker Thread: " + counter.incrementAndGet());
      try
      {
         for (; ; )
         {
            if (shutdown) break;
            try
            {
               SelectorUtil.select(selector);
               if (shutdown) break;
               processRegistrations();
               processReads();

               synchronized (registrationQueue)
               {
                  if (selector.keys().isEmpty())
                  {
                     // go to sleep if not managing any more connections
                     idle = true;
                     try
                     {
                        registrationQueue.wait();
                        if (shutdown) break;
                     }
                     catch (InterruptedException e)
                     {
                        break;
                     }
                     idle = false;
                  }
               }
            }
            catch (Throwable t)
            {
               logger.warn(
                       "Unexpected exception in the selector loop.", t);

               // Prevent possible consecutive immediate failures that lead to
               // excessive CPU consumption.
               try
               {
                  Thread.sleep(1000);
               }
               catch (InterruptedException e)
               {
                  // Ignore.
               }

            }
         }
      }
      finally
      {
         SelectorUtil.cleanupThreadSelector();
         shutdownLatch.countDown();
         Thread.currentThread().setName(oldName);
      }
   }

   public void close()
   {
      Set<SelectionKey> keys = selector.keys();
      for (SelectionKey key : keys)
      {
         try
         {
            ((ManagedChannel) key.attachment()).shutdown();
            key.cancel();
         }
         catch (Exception ignored)
         {

         }
      }
      SelectorUtil.safeClose(selector);
   }
}
