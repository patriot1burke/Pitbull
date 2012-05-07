package org.jboss.pitbull.internal.nio.socket;

import org.jboss.pitbull.ReadTimeoutException;
import org.jboss.pitbull.internal.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.*;

/**
 * An input stream which reads from a stream source channel with a buffer.  It will only read a total fixed length
 * set of total bytes.  In addition, the
 * {@link #available()} method can be used to determine whether the next read will or will not block.
 *
 * @apiviz.exclude
 * @since 2.1
 */
public class BufferedBlockingInputStream extends InputStream
{
   private final ManagedChannel channel;
   private final ByteBuffer buffer;
   private volatile boolean closed;
   protected static final Logger log = Logger.getLogger(BufferedBlockingInputStream.class);

   /**
    * Construct a new instance.
    *
    * @param channel    the channel to wrap
    * @param bufferSize the size of the internal buffer
    */
   public BufferedBlockingInputStream(final ManagedChannel channel, ByteBuffer buffer)
   {
      if (channel == null)
      {
         throw new NullPointerException("channel is null");
      }
      if (buffer == null)
      {
         throw new NullPointerException("buffer is null");
      }
      this.buffer = buffer;
      this.channel = channel;
   }

   protected volatile long timeout;

   /**
    * Get the read timeout.
    *
    * @param unit the time unit
    * @return the timeout in the given unit
    */
   public long getReadTimeout(TimeUnit unit)
   {
      return unit.convert(timeout, TimeUnit.MILLISECONDS);
   }

   /**
    * Set the read timeout.  Does not affect read operations in progress.
    *
    * @param timeout the read timeout, or 0 for none
    * @param unit    the time unit
    */
   public void setReadTimeout(long timeout, TimeUnit unit)
   {
      if (timeout < 0L)
      {
         throw new IllegalArgumentException("Negative timeout");
      }
      final long calcTimeout = unit.toMillis(timeout);
      this.timeout = timeout == 0L ? 0L : calcTimeout < 1L ? 1L : calcTimeout;
   }

   protected void resetBufferLimit()
   {
      buffer.clear();
   }

   /**
    * Read a byte, blocking if necessary.
    *
    * @return the byte read, or -1 if the end of the stream has been reached
    * @throws java.io.IOException if an I/O error occurs
    */
   public int read() throws IOException
   {
      if (closed) return -1;

      final ByteBuffer buffer = this.buffer;

      final long timeout = this.timeout;
      if (timeout == 0L)
      {
         while (!buffer.hasRemaining())
         {
            buffer.clear();
            final int res = channel.readBlocking(buffer);
            if (res == -1)
            {
               return -1;
            }
            buffer.flip();
         }
      }
      else
      {
         if (!buffer.hasRemaining())
         {
            long now = System.currentTimeMillis();
            final long deadline = timeout - now;
            do
            {
               resetBufferLimit();
               if (deadline <= now)
               {
                  throw new ReadTimeoutException("Read timed out");
               }
               final int res = channel.readBlocking(buffer, deadline - now, TimeUnit.MILLISECONDS);
               if (res == -1)
               {
                  return -1;
               }
               buffer.flip();
            } while (!buffer.hasRemaining());
         }
      }
      return buffer.get() & 0xff;
   }

   /**
    * Read bytes into an array.
    *
    * @param b   the destination array
    * @param off the offset into the array at which bytes should be filled
    * @param len the number of bytes to fill
    * @return the number of bytes read, or -1 if the end of the stream has been reached
    * @throws java.io.IOException if an I/O error occurs
    */
   public int read(final byte[] b, int off, int len) throws IOException
   {
      if (closed) return -1;
      if (len < 1)
      {
         return 0;
      }
      int total = 0;
      final ByteBuffer buffer = this.buffer;
      if (buffer.hasRemaining())
      {
         final int cnt = min(buffer.remaining(), len);
         buffer.get(b, off, cnt);
         total += cnt;
         off += cnt;
         len -= cnt;
      }
      if (closed) return -1;
      if (len <= 0) return total;


      final long timeout = this.timeout;
      try
      {
         if (timeout == 0L)
         {
            final ByteBuffer dst = ByteBuffer.wrap(b, off, len);
            int res = total > 0 ? channel.read(dst) : channel.readBlocking(dst);
            if (res == -1)
            {
               return total == 0 ? -1 : total;
            }
            else if (res == 0)
            {
               return total;
            }
            else
            {
               total += res;
               return total;
            }
         }
         else
         {
            final ByteBuffer dst = ByteBuffer.wrap(b, off, len);
            int res;
            if (total > 0)
            {
               res = channel.read(dst);
            }
            else
            {
               res = channel.readBlocking(dst, timeout, TimeUnit.MILLISECONDS);
               if (res == 0)
               {
                  throw new ReadTimeoutException("Read timed out");
               }
            }
            if (res == -1)
            {
               return total == 0 ? -1 : total;
            }
            else if (res == 0)
            {
               return total;
            }
            else
            {
               total += res;
               return total;
            }
         }
      }
      catch (InterruptedIOException e)
      {
         e.bytesTransferred = total;
         throw e;
      }
   }

   /**
    * Skip bytes in the stream.
    *
    * @param n the number of bytes to skip
    * @return the number of bytes skipped (0 if the end of stream has been reached)
    * @throws java.io.IOException if an I/O error occurs
    */
   public long skip(long n) throws IOException
   {
      if (closed) return 0L;
      if (n < 1L)
      {
         return 0L;
      }
      long total = 0L;
      final ByteBuffer buffer = this.buffer;
      if (buffer.hasRemaining())
      {
         final int cnt = (int) min(buffer.remaining(), n);
         ByteBuffers.skip(buffer, cnt);
         total += cnt;
         n -= cnt;
      }
      if (closed)
      {
         return total;
      }
      if (n > 0L)
      {
         // Buffer was cleared
         try
         {
            while (n > 0L)
            {
               resetBufferLimit();
               int res = total > 0L ? channel.read(buffer) : channel.readBlocking(buffer);
               if (res <= 0)
               {
                  return total;
               }
               total += (long) res;
            }
         }
         finally
         {
            buffer.position(0).limit(0);
         }
      }
      return total;
   }

   public int bufferAvailable()
   {
      return buffer.remaining();
   }

   /**
    * Return the number of bytes available to read, or 0 if a subsequent {@code read()} operation would block.
    *
    * @return the number of ready bytes, or 0 for none
    * @throws java.io.IOException if an I/O error occurs
    */
   public int available() throws IOException
   {
      final ByteBuffer buffer = this.buffer;
      final int rem = buffer.remaining();
      if (rem > 0 || closed)
      {
         return rem;
      }
      resetBufferLimit();
      try
      {
         channel.read(buffer);
      }
      catch (IOException e)
      {
         buffer.limit(0);
         throw e;
      }
      buffer.flip();
      return buffer.remaining();
   }

   /**
    * Close the stream.  Shuts down the channel's read side.
    *
    * @throws java.io.IOException if an I/O error occurs
    */
   public void close() throws IOException
   {
      closed = true;
   }
}
