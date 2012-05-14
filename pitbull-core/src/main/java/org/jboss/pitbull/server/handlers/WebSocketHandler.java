package org.jboss.pitbull.server.handlers;

import org.jboss.pitbull.server.spi.RequestHandler;
import org.jboss.pitbull.websocket.WebSocket;

import java.io.IOException;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author Mike Brock
 * @version $Revision: 1 $
 */
public interface WebSocketHandler extends RequestHandler
{
   /**
    * Name of the websocket protocol.  Returned with initial connection.  May be null.
    *
    * @return
    */
   String getProtocolName();

   void onReceivedFrame(WebSocket socket) throws IOException;
}
