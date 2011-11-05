package org.jboss.pitbull.nio.socket;

import org.jboss.pitbull.logging.Logger;

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
               logger.debug("Cleaned up ThreadLocal selector");
            }
         }
      };
      return new Thread(delegate);
   }
}