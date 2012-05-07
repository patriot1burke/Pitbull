package org.jboss.pitbull.internal.nio.socket;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;


/**
 * Works the same as BufferedOutputStream except it invokes a callback prior to:
 * - initial flush of buffer
 * - subsequent flush of buffer
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class BufferedBlockingOutputStream extends OutputStream
{
   protected ManagedChannel channel;
   protected ByteBuffer buffer;
   protected int size;
   protected boolean closed;
   protected long timeout;
   protected boolean committed = true;

   /**
    * delegate OutputStream can be null and set at another time (i.e. at initialFlush time)
    *
    * @param out
    */
   public BufferedBlockingOutputStream(ManagedChannel channel)
   {
      this(channel, 8192);
   }

   /**
    * delegate OutputStream can be null and set at another time (i.e. at initialFlush time)
    *
    * @param out
    * @param size must be > 0
    */
   public BufferedBlockingOutputStream(ManagedChannel channel, int size)
   {
      this.channel = channel;
      setBufferSize(size);
   }

   protected void flushBuffer() throws IOException
   {
      if (buffer.position() > 0)
      {
         buffer.flip();
         writeMessage(buffer);
         buffer.clear();
         committed = true;
      }
   }

   public void reset() throws IllegalStateException
   {
      if (committed == false) throw new IllegalStateException("Buffer was flushed");
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
      flushBuffer();
      closed = true;
   }

   protected void checkClosed() throws IOException
   {
      if (closed) throw new IOException("Stream is closed.");
   }
}

