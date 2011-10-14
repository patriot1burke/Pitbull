package org.jboss.pitbull.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.pitbull.spi.ResponseHeader;

import java.io.IOException;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;

/**
 * Works the same as BufferedOutputStream except it invokes a callback prior to:
 * - initial flush of buffer
 * - subsequent flush of buffer
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class BufferedContentOutputStream extends NettyContentOutputStream
{
   protected Channel channel;
   protected ChannelBuffer buffer;
   protected boolean initialFlush = true;
   protected int size;
   protected ChannelFuture lastFuture;
   protected ResponseHeader responseHeader;
   protected boolean closed;

   /**
    * delegate OutputStream can be null and set at another time (i.e. at initialFlush time)
    *
    * @param out
    */
   public BufferedContentOutputStream(Channel channel, ResponseHeader responseHeader)
   {
      this(channel, responseHeader, 8192);
   }

   /**
    * delegate OutputStream can be null and set at another time (i.e. at initialFlush time)
    *
    * @param out
    * @param size must be > 0
    */
   public BufferedContentOutputStream(Channel channel, ResponseHeader responseHeader, int size)
   {
      this.channel = channel;
      this.responseHeader = responseHeader;
      setBufferSize(size);
   }

   public ChannelFuture getLastFuture()
   {
      return lastFuture;
   }

   private void flushBuffer(boolean isClosing) throws IOException
   {
      if (initialFlush)
      {
         initialFlush = false;
         ChannelBuffer tmp = buffer;
         buffer = ChannelBuffers.buffer(size);

         HttpResponse response = NettyStreamResponseWriter.createResponse(responseHeader);

         if (isClosing)
         {
            if (tmp.readableBytes() > 0) // set content-length header and full content
            {
               response.setHeader(CONTENT_LENGTH, tmp.readableBytes());
               response.setContent(tmp);
               writeMessage(response);
            }
         }
         else // set transfer-encoding header and first HttpChunk
         {
            response.removeHeader(CONTENT_LENGTH);
            response.addHeader(TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
            writeMessage(response);
            DefaultHttpChunk chunk = new DefaultHttpChunk(tmp);
            writeMessage(chunk);
         }
      }
      else
      {
         if (buffer.readableBytes() > 0)
         {
            ChannelBuffer tmp = buffer;
            buffer = ChannelBuffers.buffer(size);
            DefaultHttpChunk chunk = new DefaultHttpChunk(tmp);
            writeMessage(chunk);
         }
         if (isClosing)
         {
            writeMessage(HttpChunk.LAST_CHUNK);
         }
      }
   }

   public void reset() throws IllegalStateException
   {
      if (initialFlush == false) throw new IllegalStateException("Buffer was flushed");
      buffer.clear();
   }

   public int getBufferSize()
   {
      return size;
   }

   public void setBufferSize(int size) throws IllegalStateException
   {
      if (size <= 0)
      {
         throw new IllegalArgumentException("Cannot set a buffer size that is less than zero.");
      }
      if (buffer != null && buffer.readableBytes() > 0) throw new IllegalStateException("Buffer has been written to, cannot reset size");
      this.size = size;
      buffer = ChannelBuffers.buffer(size);
   }


   public synchronized void write(int b) throws IOException
   {
      checkClosed();
      if (buffer.writableBytes() <= 0)
      {
         flushBuffer(false);
      }
      buffer.writeByte(b);
   }


   public synchronized void write(byte b[], int off, int len) throws IOException
   {
      checkClosed();
      if (len >= size)
      {
         flushBuffer(false);
         ChannelBuffer tmp = ChannelBuffers.wrappedBuffer(b, off, len);
         writeMessage(tmp);
         return;
      }

      if (len > buffer.writableBytes())
      {
         flushBuffer(false);
      }
      buffer.writeBytes(b, off, len);
   }

   protected void writeMessage(Object tmp) throws IOException
   {
      lastFuture = channel.write(tmp);
      boolean success = false;
      try
      {
         success = lastFuture.await().isSuccess();
         if (!success)
         {
            throw new IOException("Failed to write", lastFuture.getCause());
         }
      }
      catch (InterruptedException e)
      {
         throw new IOException("Write was interrupted", e);
      }
   }


   public synchronized void flush() throws IOException
   {
      checkClosed();
      flushBuffer(false);
   }

   public synchronized void close() throws IOException
   {
      flushBuffer(true);
      closed = true;
   }

   protected void checkClosed() throws IOException
   {
      if (closed) throw new IOException("Stream is closed.");
   }

   @Override
   public boolean isCommitted()
   {
      return !initialFlush;
   }
}

