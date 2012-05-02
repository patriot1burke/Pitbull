package org.jboss.pitbull.internal;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class NotImplementedYetException extends RuntimeException
{
   public NotImplementedYetException()
   {
   }

   public NotImplementedYetException(String s)
   {
      super(s);
   }

   public NotImplementedYetException(String s, Throwable throwable)
   {
      super(s, throwable);
   }

   public NotImplementedYetException(Throwable throwable)
   {
      super(throwable);
   }
}
