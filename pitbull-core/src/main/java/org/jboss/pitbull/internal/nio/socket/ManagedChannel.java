package org.jboss.pitbull.internal.nio.socket;

import org.jboss.pitbull.PitbullChannel;

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
public class ManagedChannel implements PitbullChannel
{
   protected Worker worker;
   protected SelectionKey key;
   protected EventHandler handler;
   protected PitbullChannel freeChannel;

   public ManagedChannel(PitbullChannel channel, EventHandler handler)
   {
      this.freeChannel = channel;
      this.handler = handler;
   }

   public void bindSelectionKey(Worker worker, SelectionKey key)
   {
      this.worker = worker;
      this.key = key;
   }

   public EventHandler getHandler()
   {
      return handler;
   }

   public void suspendReads()
   {
      if (!worker.inWorkerThread())
         throw new IllegalStateException("Can only be called within worker thread at this time");
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

   public String getId()
   {
      return freeChannel.getId();
   }

   public SSLSession getSslSession()
   {
      return freeChannel.getSslSession();
   }

   public SocketChannel getChannel()
   {
      return freeChannel.getChannel();
   }

   public PitbullChannel getFreeChannel()
   {
      return freeChannel;
   }

   public int read(ByteBuffer buf) throws IOException
   {
      return freeChannel.read(buf);
   }

   public int readBlocking(ByteBuffer buf) throws IOException
   {
      return freeChannel.readBlocking(buf);
   }

   public int readBlocking(ByteBuffer buf, long time, TimeUnit unit) throws IOException
   {
      return freeChannel.readBlocking(buf, time, unit);
   }

   public int write(ByteBuffer buf) throws IOException
   {
      return freeChannel.write(buf);
   }

   public int writeBlocking(ByteBuffer buffer) throws IOException
   {
      return freeChannel.writeBlocking(buffer);
   }

   public int writeBlocking(ByteBuffer buffer, long time, TimeUnit unit) throws IOException
   {
      return freeChannel.writeBlocking(buffer, time, unit);
   }

   public boolean isClosed()
   {
      return freeChannel.isClosed();
   }

   public void close()
   {
      freeChannel.close();
      try
      { if (key != null) key.cancel(); }
      catch (Exception ignored)
      {}
   }

   public void shutdown()
   {
      handler.shutdown();
      close();
   }

}
