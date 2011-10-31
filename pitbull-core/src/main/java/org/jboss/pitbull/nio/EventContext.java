package org.jboss.pitbull.nio;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class EventContext
{
   protected final SelectionKey key;
   protected final ManagedChannel channel;

   public EventContext(SelectionKey key, ManagedChannel channel)
   {
      this.key = key;
      this.channel = channel;
   }

   public ManagedChannel getChannel()
   {
      return channel;
   }

   public void suspendReads()
   {
      key.interestOps(0);
   }

   public void resumeReads()
   {
      key.interestOps(SelectionKey.OP_READ);
   }
}
