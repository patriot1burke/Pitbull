package org.jboss.pitbull.netty;

import org.jboss.pitbull.spi.Connection;

import javax.net.ssl.SSLSession;
import java.net.SocketAddress;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class NettyConnection implements Connection
{
   private int id;
   private SocketAddress localAddress;
   private SocketAddress remoteAddress;
   private SSLSession sslSession;
   private boolean secure;

   public NettyConnection(int id, SocketAddress localAddress, SocketAddress remoteAddress, SSLSession sslSession, boolean secure)
   {
      this.id = id;
      this.localAddress = localAddress;
      this.remoteAddress = remoteAddress;
      this.sslSession = sslSession;
      this.secure = secure;
   }

   @Override
   public int getId()
   {
      return id;
   }

   @Override
   public SocketAddress getLocalAddress()
   {
      return localAddress;
   }

   @Override
   public SocketAddress getRemoteAddress()
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
