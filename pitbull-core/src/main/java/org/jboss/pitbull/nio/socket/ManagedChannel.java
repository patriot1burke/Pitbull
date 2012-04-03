package org.jboss.pitbull.nio.socket;

import org.jboss.pitbull.logging.Logger;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

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
   protected static final Logger logger = Logger.getLogger(ManagedChannel.class);
   protected SSLSession sslSession;

   public ManagedChannel(SocketChannel channel, EventHandler handler)
   {
      this.channel = channel;
      this.handler = handler;
   }

   public void bindSelectionKey(SelectionKey key)
   {
      this.key = key;
   }

   public SSLSession getSslSession()
   {
      return sslSession;
   }

   public SocketChannel getChannel()
   {
      return channel;
   }

   public EventHandler getHandler()
   {
      return handler;
   }

   /**
    * Non-blocking
    *
    * @param buf
    * @return
    * @throws IOException
    */
   public int read(ByteBuffer buf) throws IOException
   {
      return channel.read(buf);
   }

   public int readBlocking(ByteBuffer buf) throws IOException
   {
      return Channels.readBlocking(getChannel(), buf);
   }

   public int readBlocking(ByteBuffer buf, long time, TimeUnit unit) throws IOException
   {
      return Channels.readBlocking(getChannel(), buf, time, unit);
   }

   /**
    * Non-blocking
    *
    * @param buf
    * @return
    * @throws IOException
    */
   public int write(ByteBuffer buf) throws IOException
   {
      return channel.write(buf);
   }

   public int writeBlocking(ByteBuffer buffer) throws IOException
   {
      return Channels.writeBlocking(getChannel(), buffer);
   }

   public int writeBlocking(ByteBuffer buffer, long time, TimeUnit unit) throws IOException
   {
      return Channels.writeBlocking(getChannel(), buffer, time, unit);
   }

   public void suspendReads()
   {
      key.interestOps(0);
   }

   public void resumeReads()
   {
      key.interestOps(SelectionKey.OP_READ);
      key.selector().wakeup();
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
      try
      { channel.close(); }
      catch (Throwable ignored)
      {}
   }

}
