package org.jboss.pitbull.internal.nio.socket;

import org.jboss.pitbull.internal.logging.Logger;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayDeque;
import java.util.Iterator;
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
   protected Queue<ManagedChannel> registrationQueue = new ArrayDeque<ManagedChannel>(10);
   protected static final Logger logger = Logger.getLogger(Worker.class);
   protected static final AtomicInteger counter = new AtomicInteger();
   protected long numRegistered;

   public Worker() throws IOException
   {
      this.selector = Selector.open();
   }

   public long getNumRegistered()
   {
      return numRegistered;
   }

   public void clearMetrics()
   {
      numRegistered = 0;
   }

   public void register(ManagedChannel channel) throws Exception
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

   protected void executeRegistration(ManagedChannel channel)
   {
      try
      {
         logger.trace("Registered channel.");
         numRegistered++;
         channel.getChannel().configureBlocking(false);
         SelectionKey key = channel.getChannel().register(selector, SelectionKey.OP_READ);
         channel.bindSelectionKey(key);
         key.attach(channel);
      }
      catch (Exception e)
      {
         logger.error("Failed to execute socket registration", e);
         try { channel.close(); } catch (Exception ignored) {}
      }
   }

   protected void processRegistrations()
   {
      for (; ; )
      {
         ManagedChannel channel = null;
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
      Set<SelectionKey> selectedKeys = selector.selectedKeys();
      for (Iterator<SelectionKey> i = selectedKeys.iterator(); i.hasNext(); )
      {
         SelectionKey key = i.next();
         i.remove();
         int readyOps = key.readyOps();
         if ((readyOps & SelectionKey.OP_READ) != 0 || readyOps == 0)
         {
            ManagedChannel channel = (ManagedChannel) key.attachment();
            try
            {
               channel.getHandler().handleRead(channel);
            }
            catch (Exception e)
            {
               logger.error("Error reading channel: ", e);
               channel.close();
            }
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
