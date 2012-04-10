package org.jboss.pitbull.internal.nio.socket;

import org.jboss.pitbull.internal.logging.Logger;

import java.util.concurrent.ThreadFactory;

/**
 * Closes threadlocal selector on thread exit
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ExecutorThreadFactory implements ThreadFactory
{
   public static final ExecutorThreadFactory singleton = new ExecutorThreadFactory();
   protected static final Logger logger = Logger.getLogger(Worker.class);

   @Override
   public Thread newThread(Runnable runnable)
   {
      final Runnable run = runnable;
      Runnable delegate = new Runnable()
      {
         @Override
         public void run()
         {
            try
            {
               run.run();
            }
            finally
            {
               SelectorUtil.cleanupThreadSelector();
               logger.trace("Cleaned up ThreadLocal selector");
            }
         }
      };
      return new Thread(delegate);
   }
}
