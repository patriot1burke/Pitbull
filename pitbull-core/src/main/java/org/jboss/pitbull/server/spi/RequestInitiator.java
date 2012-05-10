package org.jboss.pitbull.server.spi;

import org.jboss.pitbull.Connection;
import org.jboss.pitbull.RequestHeader;

/**
 * RequestInitiators exist so that a subsystem can pick different types of handlers to use based on properties
 * (path, request headers, etc.) of the
 * incoming request.  i.e. The subsystem may want a deep copy request handler for static content stored on disk.
 * For dynamic content, the subsystem may want a StreamRequestHandler.  For web sockets, a web socket handler.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface RequestInitiator
{
   /**
    * Called when a new HTTP request is available for processing.  Container returns a RequestHandler that implements
    * one or more RequestHandler sub-interfaces.
    *
    * @param connection
    * @param header     method, uri, and request headers
    * @return
    */
   RequestHandler begin(Connection connection, RequestHeader requestHeader);

   /**
    * Called if Pitbull doesn't know how to interact with a specific handler returned from begin()
    *
    * @param handler
    */
   void illegalHandler(RequestHandler handler);
}
