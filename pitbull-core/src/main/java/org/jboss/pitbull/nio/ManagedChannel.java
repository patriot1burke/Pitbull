package org.jboss.pitbull.nio;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ManagedChannel
{
   protected SocketChannel channel;
   protected SelectionKey key;
   protected EventHandler handler;
   protected boolean closed;

   public ManagedChannel(SocketChannel channel, SelectionKey key, EventHandler handler)
   {
      this.channel = channel;
      this.key = key;
      this.handler = handler;
   }

   public SocketChannel getChannel()
   {
      return channel;
   }

   public EventHandler getHandler()
   {
      return handler;
   }

   public void suspendReads()
   {
      key.interestOps(0);
   }

   public void resumeReads()
   {
      key.interestOps(SelectionKey.OP_READ);
   }

   public void shutdown()
   {
      handler.shutdown();
      close();
   }

   public boolean isClosed()
   {
      return closed;
   }

   public void close()
   {
      if (closed) return;
      closed = true;
      try { channel.close(); } catch (Throwable ignored) {}
   }

}
