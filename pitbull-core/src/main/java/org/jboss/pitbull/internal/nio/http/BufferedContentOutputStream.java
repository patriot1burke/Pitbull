package org.jboss.pitbull.internal.nio.http;

import org.jboss.pitbull.ContentOutputStream;
import org.jboss.pitbull.OrderedHeaders;
import org.jboss.pitbull.PitbullChannel;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class BufferedContentOutputStream extends ContentOutputStream
{
   static final byte[] CRLF = new byte[]{HttpRequestDecoder.CR, HttpRequestDecoder.LF};
   static byte[] LAST_CHUNK;


   {
      try
      {
         LAST_CHUNK = "0\r\n\r\n".getBytes("ASCII");
      }
      catch (UnsupportedEncodingException e)
      {
         throw new RuntimeException(e);
      }
   }

   public interface ContentMessage
   {
      OrderedHeaders getHeaders();
      byte[] getMessageBytes() throws IOException;
      void prepareEmptyBody();
   }


   protected PitbullChannel channel;
   protected ByteBuffer buffer;
   protected boolean initialFlush = true;
   protected int size;
   protected ContentMessage contentMessage;
   protected boolean closed;
   protected long timeout;

   public BufferedContentOutputStream(PitbullChannel channel, int bufferSize)
   {
      this.channel = channel;
      this.size = bufferSize;
      setBufferSize(size);
   }

   private void flushBuffer() throws IOException
   {
      if (initialFlush)
      {
         if (buffer.position() > 0)
         {
            initialFlush = false;
            contentMessage.getHeaders().removeHeader("Content-Length");
            contentMessage.getHeaders().addHeader("Transfer-Encoding", "chunked");
            writeContentMessage();
            buffer.flip();
            writeChunk(buffer);
            buffer.clear();
         }
         else // if there is nothing to write from buffer, just keep the state in initial
         {
            initialFlush = true;
         }
      }
      else
      {
         if (buffer.position() > 0)
         {
            buffer.flip();
            writeChunk(buffer);
            buffer.clear();
         }
      }
   }

   private void writeChunk(ByteBuffer buf) throws IOException
   {
      int len = buf.remaining();
      String hex = Integer.toHexString(len);
      StringBuilder builder = new StringBuilder(hex.length() + 2);
      builder.append(hex).append("\r\n");
      writeMessage(ByteBuffer.wrap(builder.toString().getBytes("ASCII")));
      writeMessage(buf);
      writeMessage(ByteBuffer.wrap(CRLF));
   }

   private void writeContentMessage() throws IOException
   {
      byte[] bytes = contentMessage.getMessageBytes();
      ByteBuffer tmp = ByteBuffer.wrap(bytes);
      writeMessage(tmp);
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
      if (buffer != null && buffer.position() > 0)
         throw new IllegalStateException("Buffer has been written to, cannot reset size");
      this.size = size;
      buffer = ByteBuffer.allocate(size);
   }

   public synchronized void write(int b) throws IOException
   {
      checkClosed();
      if (buffer.hasRemaining())
      {
         flushBuffer();
      }
      buffer.put((byte) b);
   }

   public synchronized void write(byte b[], int off, int len) throws IOException
   {
      checkClosed();
      if (len >= size)
      {
         flushBuffer();
         ByteBuffer tmp = ByteBuffer.wrap(b, off, len);
         writeMessage(tmp);
         return;
      }

      if (len > buffer.remaining())
      {
         flushBuffer();
      }
      buffer.put(b, off, len);
   }

   protected void writeMessage(ByteBuffer tmp) throws IOException
   {
      final long timeout = this.timeout;
      int total = 0;
      if (timeout == 0L)
      {
         try
         {
            total = channel.writeBlocking(tmp);
         }
         catch (InterruptedIOException e)
         {
            e.bytesTransferred = tmp.position();
            throw e;
         }
      }
      else
      {
         try
         {
            total = channel.writeBlocking(tmp, timeout, TimeUnit.MILLISECONDS);
         }
         catch (InterruptedIOException e)
         {
            e.bytesTransferred = tmp.position();
            throw e;
         }
      }
   }

   public synchronized void flush() throws IOException
   {
      checkClosed();
      flushBuffer();
   }

   public synchronized void close() throws IOException
   {
      if (closed) return;
      closed = true;
      if (initialFlush)
      {
         initialFlush = false;

         if (buffer.position() > 0) // set content-length header and full content
         {
            buffer.flip();
            contentMessage.getHeaders().removeHeader("Content-Length");
            contentMessage.getHeaders().addHeader("Content-Length", Integer.toString(buffer.remaining()));
            writeContentMessage();
            writeMessage(buffer);
            buffer.clear();
         }
         else // we have nothing in buffer
         {
            contentMessage.prepareEmptyBody();
            writeContentMessage();
         }
      }
      else
      {
         if (buffer.position() > 0)
         {
            buffer.flip();
            writeChunk(buffer);
            buffer.clear();
         }
         writeMessage(ByteBuffer.wrap(LAST_CHUNK));
      }
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
