package org.jboss.pitbull;

import java.io.IOException;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ReadTimeoutException extends IOException
{
   public ReadTimeoutException()
   {
   }

   public ReadTimeoutException(String s)
   {
      super(s);
   }

   public ReadTimeoutException(String s, Throwable throwable)
   {
      super(s, throwable);
   }

   public ReadTimeoutException(Throwable throwable)
   {
      super(throwable);
   }
}
