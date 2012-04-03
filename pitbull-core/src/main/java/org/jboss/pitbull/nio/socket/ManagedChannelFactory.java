package org.jboss.pitbull.nio.socket;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ManagedChannelFactory
{
   protected EventHandlerFactory eventHandlerFactory;

   public ManagedChannelFactory(EventHandlerFactory eventHandlerFactory)
   {
      this.eventHandlerFactory = eventHandlerFactory;
   }

   public ManagedChannel create(SocketChannel channel) throws Exception
   {
      return new ManagedChannel(channel, eventHandlerFactory.create());
   }
}
