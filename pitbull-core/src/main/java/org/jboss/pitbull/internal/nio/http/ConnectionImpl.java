package org.jboss.pitbull.internal.nio.http;

import org.jboss.pitbull.Connection;

import javax.net.ssl.SSLSession;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ConnectionImpl implements Connection
{
   private String id;
   private InetSocketAddress localAddress;
   private InetSocketAddress remoteAddress;
   private SSLSession sslSession;
   private boolean secure;


   public ConnectionImpl(SocketAddress localAddress, SocketAddress remoteAddress, SSLSession sslSession, boolean secure)
   {
      this(UUID.randomUUID().toString(), localAddress, remoteAddress, sslSession, secure);
   }


   public ConnectionImpl(String id, SocketAddress localAddress, SocketAddress remoteAddress, SSLSession sslSession, boolean secure)
   {
      this.id = id;
      this.localAddress = (InetSocketAddress) localAddress;
      this.remoteAddress = (InetSocketAddress) remoteAddress;
      this.sslSession = sslSession;
      this.secure = secure;
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public InetSocketAddress getLocalAddress()
   {
      return localAddress;
   }

   @Override
   public InetSocketAddress getRemoteAddress()
   {
      return remoteAddress;
   }

   @Override
   public SSLSession getSSLSession()
   {
      return sslSession;
   }

   @Override
   public boolean isSecure()
   {
      return secure;
   }
}
