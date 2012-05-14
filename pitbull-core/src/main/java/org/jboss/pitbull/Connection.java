package org.jboss.pitbull;

import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface Connection
{
   String getId();

   InetSocketAddress getLocalAddress();

   InetSocketAddress getRemoteAddress();

   boolean isSecure();
}
