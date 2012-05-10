package org.jboss.pitbull.internal.nio.websocket.impl.oio;

import java.io.InputStream;

/**
 * This is an abstraction of an http request so that this code can be re-used in different HTTP Engines (i.e. servlet).
 * Instances of this class usually delegate to the underyling container's implementation of an http request.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface HttpRequestBridge
{
   boolean isSecure();
   String getHeader(String name);
   String getRequestURI();
   InputStream getInputStream();
}
