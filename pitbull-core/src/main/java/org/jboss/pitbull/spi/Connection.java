package org.jboss.pitbull.spi;

import javax.net.ssl.SSLSession;
import java.net.SocketAddress;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface Connection
{
   int getId();
   SocketAddress getLocalAddress();
   SocketAddress getRemoteAddress();
   SSLSession getSSLSession();
   boolean isSecure();

}
