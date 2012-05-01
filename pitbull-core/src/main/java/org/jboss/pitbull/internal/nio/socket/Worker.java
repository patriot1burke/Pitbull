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
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class Worker implements Runnable
{
   interface Event
   {
      void execute();
   }

   protected Selector selector;
   protected volatile boolean shutdown;
   protected volatile boolean idle;
   protected CountDownLatch shutdownLatch = new CountDownLatch(1);
   protected Queue<FutureTask> eventQueue = new ArrayDeque<FutureTask>(10);
   protected static final Logger logger = Logger.getLogger(Worker.class);
   protected static final AtomicInteger counter = new AtomicInteger();
   protected long numRegistered;
   protected Thread workerThread;

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

   public boolean inWorkerThread()
   {
      if (workerThread == null) return false;
      return workerThread == Thread.currentThread();
   }

   public Future queueEvent(final Runnable runnable)
   {
      FutureTask futureTask = new FutureTask(runnable, null);
      synchronized (eventQueue)
      {
         eventQueue.add(futureTask);
         if (idle)
         {
            eventQueue.notify();
         }
         else
         {
            selector.wakeup();
         }
      }
      return futureTask;
   }


   public void queueRegistration(final ManagedChannel channel)
   {
      Runnable runnable = new Runnable()
      {
         @Override
         public void run()
         {
            executeRegistration(channel);
         }
      };
      queueEvent(runnable);
   }

   protected void executeRegistration(ManagedChannel channel)
   {
      try
      {
         logger.trace("Registered channel.");
         numRegistered++;
         channel.getChannel().configureBlocking(false);
         SelectionKey key = channel.getChannel().register(selector, SelectionKey.OP_READ);
         channel.bindSelectionKey(this, key);
         key.attach(channel);
      }
      catch (Exception e)
      {
         logger.error("Failed to execute socket registration", e);
         try
         { channel.close(); }
         catch (Exception ignored)
         {}
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
               logger.trace("Error reading channel: ", e);
               channel.close();
            }
            if (!channel.getChannel().isOpen())
            {
               key.cancel();
            }
         }
      }
   }

   public boolean isShutdown()
   {
      return shutdown;
   }

   public void shutdown()
   {
      synchronized (eventQueue)
      {
         shutdown = true;
         eventQueue.notify();
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
      workerThread = Thread.currentThread();
      try
      {
         for (; ; )
         {
            if (shutdown) break;
            try
            {
               SelectorUtil.select(selector);
               if (shutdown) break;

               synchronized (eventQueue)
               {
                  // Empty all events
                  for (FutureTask event = eventQueue.poll(); event != null; event = eventQueue.poll())
                  {
                     event.run();
                  }

                  selector.selectNow(); // events may have changed selector status

                  processReads();

                  selector.selectNow(); // reads may have changed selector status

                  if (selector.keys().isEmpty())
                  {
                     // go to sleep if not managing any more connections
                     idle = true;
                     try
                     {
                        eventQueue.wait();
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
      try
      {
         for (FutureTask event = eventQueue.poll(); event != null; event = eventQueue.poll())
         {
            event.cancel(false);
         }

      }
      catch (Exception ignore)
      {

      }

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
