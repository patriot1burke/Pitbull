package org.jboss.pitbull.client.internal;

import org.jboss.pitbull.client.ClientConnection;
import org.jboss.pitbull.client.ClientInvocation;
import org.jboss.pitbull.handlers.PitbullChannel;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ClientConnectionImpl implements ClientConnection
{
   protected PitbullChannel channel;
   protected String host;
   protected ClientResponseImpl last;

   public ClientConnectionImpl(PitbullChannel channel, String host, int port)
   {
      this.channel = channel;
      this.host = host + (port == 80 ? "" : (":"+Integer.toString(port)));

   }

   public void setLast(ClientResponseImpl last)
   {
      this.last = last;
   }

   @Override
   public String getHostHeader()
   {
      return host;
   }

   @Override
   public ClientInvocation request(String uri)
   {
      if (last != null)
      {
         final ClientResponseImpl tmp = last;
         last = null;
         try
         {
            tmp.close();
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
      }
      return new ClientInvocationImpl(this, uri);
   }

   @Override
   public boolean isClosed()
   {
      return channel.isClosed();
   }

   @Override
   public void close()
   {
      channel.close();
   }

   @Override
   public String getId()
   {
      return channel.getId();
   }

   @Override
   public InetSocketAddress getLocalAddress()
   {
      return (InetSocketAddress)(channel.getChannel().socket().getLocalSocketAddress());
   }

   @Override
   public InetSocketAddress getRemoteAddress()
   {
      return (InetSocketAddress)(channel.getChannel().socket().getRemoteSocketAddress());
   }

   @Override
   public SSLSession getSSLSession()
   {
      return channel.getSslSession();
   }

   @Override
   public boolean isSecure()
   {
      return channel.getSslSession() != null;
   }
}
