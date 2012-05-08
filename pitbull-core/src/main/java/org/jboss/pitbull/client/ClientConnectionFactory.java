package org.jboss.pitbull.client;

import java.security.KeyStore;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ClientConnectionFactory
{
   public static ClientConnection http(String host)
   {
      return null;
   }

   public static ClientConnection http(String host, int port)
   {
      return null;
   }

   public static ClientConnection http(String host, int port, long timeout, TimeUnit unit)
   {
      return null;
   }

   /**
    * Defaults to 443 port.  Will trust any certificates!
    *
    * @param host
    * @return
    */
   public static ClientConnection https(String host)
   {
      return null;
   }

   /**
    * Will trust any certificates!
    *
    * @param host
    * @param port
    * @return
    */
   public static ClientConnection https(String host, int port)
   {
      return null;
   }

   public static ClientConnection https(String host, int port, long timeout, TimeUnit unit)
   {
      return null;
   }

   public static ClientConnection https(String host, KeyStore trustStore)
   {
      return null;
   }

   public static ClientConnection https(String host, int port, KeyStore trustStore)
   {
      return null;
   }

   public static ClientConnection https(String host, int port, KeyStore trustStore, long timeout, TimeUnit unit)
   {
      return null;
   }
}
