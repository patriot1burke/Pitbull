package org.jboss.pitbull.internal.nio.socket;

import org.jboss.pitbull.internal.logging.Logger;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ManagedChannel
{
   protected SocketChannel channel;
   protected Worker worker;
   protected SelectionKey key;
   protected EventHandler handler;
   protected boolean closed;
   private static final Logger log = Logger.getLogger(ManagedChannel.class);
   protected SSLSession sslSession;
   protected static final AtomicInteger counter = new AtomicInteger();
   protected String id;

   public ManagedChannel(SocketChannel channel, EventHandler handler)
   {
      this.channel = channel;
      this.handler = handler;
      this.id = Integer.toString(counter.incrementAndGet());
   }

   public String getId()
   {
      return id;
   }

   public void bindSelectionKey(Worker worker, SelectionKey key)
   {
      this.worker = worker;
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
      if (!worker.inWorkerThread()) throw new IllegalStateException("Can only be called within worker thread at this time");
      key.interestOps(0);
   }

   public void resumeReads()
   {
      if (worker.inWorkerThread())
      {
         key.interestOps(SelectionKey.OP_READ);
         // don't need to selectNow() because Worker loop should do this after processReads.
      }
      else
      {
        worker.queueEvent(new Runnable()
        {
           @Override
           public void run()
           {
              ManagedChannel.this.resumeReads();
           }
        });
      }
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
      log.trace("Channel closed: {0}", id);
      closed = true;
      try
      { channel.close(); }
      catch (Throwable ignored)
      {}

      try { if (key != null) key.cancel(); } catch (Exception ignored) {}
   }

}
