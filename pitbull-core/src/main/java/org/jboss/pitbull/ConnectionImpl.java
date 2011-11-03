package org.jboss.pitbull;

import org.jboss.pitbull.spi.Connection;

import javax.net.ssl.SSLSession;
import java.net.SocketAddress;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ConnectionImpl implements Connection
{
   private String id;
   private SocketAddress localAddress;
   private SocketAddress remoteAddress;
   private SSLSession sslSession;
   private boolean secure;
   private static final AtomicInteger counter = new AtomicInteger();


   public ConnectionImpl(SocketAddress localAddress, SocketAddress remoteAddress, SSLSession sslSession, boolean secure)
   {
      this(UUID.randomUUID().toString(), localAddress, remoteAddress, sslSession, secure);
   }


   public ConnectionImpl(String id, SocketAddress localAddress, SocketAddress remoteAddress, SSLSession sslSession, boolean secure)
   {
      this.id = id;
      this.localAddress = localAddress;
      this.remoteAddress = remoteAddress;
      this.sslSession = sslSession;
      this.secure = secure;
   }

   @Override
   public String getId()
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
