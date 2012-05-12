package org.jboss.pitbull.client;

import org.jboss.pitbull.PitbullChannel;
import org.jboss.pitbull.internal.NotImplementedYetException;
import org.jboss.pitbull.internal.client.ClientConnectionImpl;
import org.jboss.pitbull.internal.client.ClientSSLChannel;
import org.jboss.pitbull.internal.nio.socket.FreeChannel;
import org.jboss.pitbull.internal.nio.socket.SelectorUtil;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HttpConnectionFactory
{
   public static ClientConnection http(String host) throws IOException
   {
      return http(host, 80);
   }

   public static ClientConnection http(String host, int port) throws IOException
   {
      return http(host, port, 0, TimeUnit.MILLISECONDS);
   }

   public static ClientConnection http(String host, int port, long timeout, TimeUnit unit) throws IOException
   {
      SocketChannel channel = createSocket(host, port, timeout, unit);
      PitbullChannel pitbullChannel = new FreeChannel(channel);
      return new ClientConnectionImpl(pitbullChannel, host, port);
   }

   private static SocketChannel createSocket(String host, int port, long timeout, TimeUnit unit) throws IOException
   {
      SocketChannel channel = SocketChannel.open();
      channel.configureBlocking(false);
      channel.connect(new InetSocketAddress(host, port));
      SelectorUtil.await(channel, SelectionKey.OP_CONNECT, timeout, unit);
      if (!channel.finishConnect())
      {
         throw new IOException("Failed to connect");
      }
      return channel;
   }

   /**
    * Defaults to 443 port.  Will trust any certificates!
    *
    * @param host
    * @return
    */
   public static ClientConnection https(String host) throws IOException
   {
      return https(host, 443);
   }

   /**
    * Will trust any certificates!
    *
    * @param host
    * @param port
    * @return
    */
   public static ClientConnection https(String host, int port) throws IOException
   {
      return https(host, port, 0, TimeUnit.MILLISECONDS);
   }

   public static ClientConnection https(String host, int port, long timeout, TimeUnit unit) throws IOException
   {
      java.lang.System.setProperty(
              "sun.security.ssl.allowUnsafeRenegotiation", "true");

      // First create a trust manager that won't care.
      X509TrustManager trustManager = new X509TrustManager()
      {
         public void checkClientTrusted(X509Certificate[] chain,
                                        String authType) throws CertificateException
         {
            // Don't do anything.
         }

         public void checkServerTrusted(X509Certificate[] chain,
                                        String authType) throws CertificateException
         {
            // Don't do anything.
         }

         public X509Certificate[] getAcceptedIssuers()
         {
            // Don't do anything.
            return null;
         }
      };
      SSLContext sslContext = null;

      try
      {
         // Now put the trust manager into an SSLContext.
         // Supported: SSL, SSLv2, SSLv3, TLS, TLSv1, TLSv1.1
         sslContext = SSLContext.getInstance("SSL");
         sslContext.init(null, new TrustManager[]{trustManager},
                 new SecureRandom());
         SSLEngine engine = sslContext.createSSLEngine();
         engine.setUseClientMode(true);
         SocketChannel channel = createSocket(host, port, timeout, unit);
         ClientSSLChannel sslChannel = new ClientSSLChannel(channel, engine);
         return new ClientConnectionImpl(sslChannel, host, port);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public static ClientConnection https(String host, KeyStore trustStore) throws IOException
   {
      throw new NotImplementedYetException();
   }

   public static ClientConnection https(String host, int port, KeyStore trustStore) throws IOException
   {
      throw new NotImplementedYetException();
   }

   public static ClientConnection https(String host, int port, KeyStore trustStore, long timeout, TimeUnit unit) throws IOException
   {
      throw new NotImplementedYetException();
   }

}
