package org.jboss.pitbull.internal.nio.socket;

import org.jboss.pitbull.internal.logging.Logger;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class Acceptor implements Runnable
{
   protected Selector selector;
   protected ServerSocketChannel channel;
   protected CountDownLatch shutdownLatch = new CountDownLatch(1);
   protected volatile boolean shutdown;
   protected final AtomicInteger workerIndex = new AtomicInteger();
   protected ManagedChannelFactory channelFactory;
   protected Worker[] workers;
   protected static final Logger logger = Logger.getLogger(Acceptor.class);
   protected static final AtomicInteger counter = new AtomicInteger();

   public Acceptor(ServerSocketChannel channel, ManagedChannelFactory channelFactory, Worker[] workers) throws IOException
   {
      this.channelFactory = channelFactory;
      this.workers = workers;
      this.channel = channel;
      selector = Selector.open();
      channel.register(selector, SelectionKey.OP_ACCEPT);
   }

   protected Worker nextWorker()
   {
      return workers[Math.abs(
              workerIndex.getAndIncrement() % workers.length)];
   }

   protected void registerAcceptedChannel(SocketChannel accepted) throws IOException
   {
      logger.trace("Accepted Connection.");
      Worker worker = nextWorker();
      try
      {
         worker.register(channelFactory.create(accepted));
      }
      catch (Exception e)
      {
         logger.error("Error registering accepted socket", e);
         try
         {
            accepted.close();
         }
         catch (Exception ignored)
         {

         }
      }
   }

   public void shutdown()
   {
      if (shutdown) return;
      try
      {
         synchronized (this) // to flush shutdown
         {
            shutdown = true;
         }
         selector.wakeup();
         shutdownLatch.await();
      }
      catch (Exception e)
      {
         logger.error("Failed to shutdown Acceptor thread", e);
      }
   }

   @Override
   public void run()
   {
      String oldName = Thread.currentThread().getName();
      Thread.currentThread().setName("Pitbull Acceptor Thread: " + counter.incrementAndGet());
      try
      {
         for (; ; )
         {
            if (shutdown) break;

            try
            {
               if (selector.select(1000) > 0)
               {
                  selector.selectedKeys().clear();
               }

               if (shutdown) break;

               SocketChannel acceptedSocket = channel.accept();
               if (acceptedSocket != null)
               {
                  registerAcceptedChannel(acceptedSocket);
               }
            }
            catch (SocketTimeoutException e)
            {
               // Thrown every second to get ClosedChannelException
               // raised.
            }
            catch (CancelledKeyException e)
            {
               // Raised by accept() when the server socket was closed.
            }
            catch (ClosedSelectorException e)
            {
               // Raised by accept() when the server socket was closed.
            }
            catch (ClosedChannelException e)
            {
               // Closed as requested.
               break;
            }
            catch (Throwable e)
            {
               logger.warn(
                       "Failed to accept a connection.", e);
               try
               {
                  Thread.sleep(1000);
               }
               catch (InterruptedException e1)
               {
                  // Ignore
               }
            }
         }
      }
      finally
      {
         closeSelector();
         shutdownLatch.countDown();
         Thread.currentThread().setName(oldName);
      }
   }

   protected void closeSelector()
   {
      try
      {
         selector.close();
      }
      catch (Exception e)
      {
         logger.warn("Failed to close a selector.", e);
      }
   }

}

