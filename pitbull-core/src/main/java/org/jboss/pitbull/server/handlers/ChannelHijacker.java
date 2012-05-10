package org.jboss.pitbull.server.handlers;

import org.jboss.pitbull.PitbullChannel;
import org.jboss.pitbull.server.spi.RequestHandler;

/**
 * A handler that allows the application to completely take over management of the socket.  Socket is unregistered
 * with any selectors currently managing the socket.  The handler is then fully responsible for cleaning up the connection.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ChannelHijacker extends RequestHandler
{
   void hijack(PitbullChannel channel);
}
