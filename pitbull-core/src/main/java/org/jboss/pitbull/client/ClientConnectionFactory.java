package org.jboss.pitbull.client;

import org.jboss.pitbull.client.internal.ClientConnectionImpl;
import org.jboss.pitbull.handlers.PitbullChannel;
import org.jboss.pitbull.internal.nio.socket.FreeChannel;
import org.jboss.pitbull.internal.nio.socket.SelectorUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ClientConnectionFactory
{
   public static ClientConnection http(String host) throws IOException
   {
      return http(host, 80);
   }

   public static ClientConnection http(String host, int port) throws IOException
   {
      SocketChannel channel = SocketChannel.open();
      channel.configureBlocking(false);
      channel.connect(new InetSocketAddress(host, port));
      SelectorUtil.await(channel, SelectionKey.OP_CONNECT);
      if (!channel.finishConnect())
      {
         throw new IOException("Failed to connect");
      }
      PitbullChannel pitbullChannel = new FreeChannel(channel);
      return new ClientConnectionImpl(pitbullChannel, host, port);
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
