package org.jboss.pitbull.spi;

import java.io.InputStream;

/**
 * This is an endpoint that is expecting to use blocking java.io streams to process an HTTP request.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface StreamHandler extends RequestHandler
{

   void execute(Connection connection, RequestHeader requestHeader, InputStream requestStream, StreamResponseWriter writer);
}
