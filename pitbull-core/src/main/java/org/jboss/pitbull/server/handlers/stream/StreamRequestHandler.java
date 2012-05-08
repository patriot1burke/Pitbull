package org.jboss.pitbull.server.handlers.stream;

import org.jboss.pitbull.Connection;
import org.jboss.pitbull.RequestHeader;
import org.jboss.pitbull.spi.RequestHandler;

import java.io.IOException;
import java.io.InputStream;

/**
 * This is an endpoint that is expecting to use blocking java.io streams to process an HTTP request.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface StreamRequestHandler extends RequestHandler
{
   void execute(Connection connection, RequestHeader requestHeader, InputStream requestStream, StreamedResponse response) throws IOException;
}
