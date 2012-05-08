package org.jboss.pitbull.client.internal;

import org.jboss.pitbull.client.ClientConnection;
import org.jboss.pitbull.client.ClientInvocation;
import org.jboss.pitbull.handlers.PitbullChannel;

import javax.net.ssl.SSLSession;
import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ClientConnectionImpl implements ClientConnection
{
   protected PitbullChannel channel;
   protected String host;
   protected int port;

   public ClientConnectionImpl(PitbullChannel channel, String host, int port)
   {
      this.channel = channel;
      this.host = host;
      this.port = port;
   }

   @Override
   public String getHost()
   {
      return host;
   }

   @Override
   public int getPort()
   {
      return port;
   }

   @Override
   public ClientInvocation request(String uri)
   {
      return null;
   }

   @Override
   public boolean isClosed()
   {
      return false;
   }

   @Override
   public void close()
   {
   }

   @Override
   public String getId()
   {
      return null;
   }

   @Override
   public InetSocketAddress getLocalAddress()
   {
      return null;
   }

   @Override
   public InetSocketAddress getRemoteAddress()
   {
      return null;
   }

   @Override
   public SSLSession getSSLSession()
   {
      return null;
   }

   @Override
   public boolean isSecure()
   {
      return false;
   }
}
