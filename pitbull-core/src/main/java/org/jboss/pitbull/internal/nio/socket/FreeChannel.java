package org.jboss.pitbull.internal.nio.socket;

import org.jboss.pitbull.handlers.PitbullChannel;
import org.jboss.pitbull.internal.logging.Logger;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class FreeChannel implements PitbullChannel
{
   private static final Logger log = Logger.getLogger(ManagedChannel.class);
   protected SocketChannel channel;
   protected boolean closed;
   protected SSLSession sslSession;
   protected String id = UUID.randomUUID().toString();

   public FreeChannel(SocketChannel channel)
   {
      this.channel = channel;
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public SSLSession getSslSession()
   {
      return sslSession;
   }

   @Override
   public SocketChannel getChannel()
   {
      return channel;
   }

   @Override
   public int read(ByteBuffer buf) throws IOException
   {
      return channel.read(buf);
   }

   @Override
   public int readBlocking(ByteBuffer buf) throws IOException
   {
      return Channels.readBlocking(getChannel(), buf);
   }

   @Override
   public int readBlocking(ByteBuffer buf, long time, TimeUnit unit) throws IOException
   {
      return Channels.readBlocking(getChannel(), buf, time, unit);
   }

   @Override
   public int write(ByteBuffer buf) throws IOException
   {
      return channel.write(buf);
   }

   @Override
   public int writeBlocking(ByteBuffer buffer) throws IOException
   {
      return Channels.writeBlocking(getChannel(), buffer);
   }

   @Override
   public int writeBlocking(ByteBuffer buffer, long time, TimeUnit unit) throws IOException
   {
      return Channels.writeBlocking(getChannel(), buffer, time, unit);
   }

   @Override
   public boolean isClosed()
   {
      return closed;
   }

   @Override
   public void close()
   {
      if (closed) return;
      log.trace("Channel closed: {0}", id);
      closed = true;
      try
      { channel.close(); }
      catch (Throwable ignored)
      {}

   }
}
