package org.jboss.pitbull.nio.http;

import org.jboss.pitbull.logging.Logger;
import org.jboss.pitbull.nio.socket.ByteBuffers;
import org.jboss.pitbull.nio.socket.Channels;
import org.jboss.pitbull.nio.socket.ManagedChannel;
import org.jboss.pitbull.nio.socket.ReadTimeoutException;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
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
public class ContentLengthInputStream extends ContentInputStream
{
   private final ManagedChannel channel;
   private final ByteBuffer buffer;
   private long remainingBytes;
   private volatile boolean closed;
   protected static final Logger log = Logger.getLogger(ContentLengthInputStream.class);

   /**
    * Construct a new instance.
    *
    * @param channel    the channel to wrap
    * @param bufferSize the size of the internal buffer
    */
   public ContentLengthInputStream(final ManagedChannel channel, ByteBuffer buffer, long contentLength)
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
      this.remainingBytes = contentLength;
   }

   protected void resetBufferLimit()
   {
      if (remainingBytes == 0)
      {
         log.trace("resetBufferLimit with 0 remaining bytes");
      }
      buffer.clear();
      if (buffer.capacity() > remainingBytes) buffer.limit((int) (remainingBytes));
   }

   @Override
   public void eat()
   {
      while (remainingBytes > 0)
      {
         try
         {
            skip(1000);
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
      }
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
      if (remainingBytes <= 0) return -1;

      final ByteBuffer buffer = this.buffer;

      final SocketChannel channel = this.channel.getChannel();
      final long timeout = this.timeout;
      if (timeout == 0L)
      {
         while (!buffer.hasRemaining())
         {
            resetBufferLimit();
            final int res = Channels.readBlocking(channel, buffer);
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
               final int res = Channels.readBlocking(channel, buffer, deadline - now, TimeUnit.MILLISECONDS);
               if (res == -1)
               {
                  return -1;
               }
               buffer.flip();
            } while (!buffer.hasRemaining());
         }
      }
      remainingBytes--;
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
      if (remainingBytes <= 0) return -1;
      if (len < 1)
      {
         return 0;
      }
      if (len > remainingBytes)
      {
         len = (int) remainingBytes;
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
         remainingBytes -= cnt;
      }
      if (closed) return -1;
      if (len <= 0) return total;


      final SocketChannel channel = this.channel.getChannel();
      final long timeout = this.timeout;
      try
      {
         if (timeout == 0L)
         {
            final ByteBuffer dst = ByteBuffer.wrap(b, off, len);
            int res = total > 0 ? channel.read(dst) : Channels.readBlocking(channel, dst);
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
               remainingBytes -= res;
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
               res = Channels.readBlocking(channel, dst, timeout, TimeUnit.MILLISECONDS);
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
               remainingBytes -= res;
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
      if (remainingBytes <= 0) return 0L;
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
         remainingBytes -= cnt;
         total += cnt;
         n -= cnt;
      }
      if (closed)
      {
         return total;
      }
      final SocketChannel channel = this.channel.getChannel();
      if (n > remainingBytes)
      {
         n = remainingBytes;
      }
      if (n > 0L)
      {
         // Buffer was cleared
         try
         {
            while (n > 0L)
            {
               resetBufferLimit();
               int res = total > 0L ? channel.read(buffer) : Channels.readBlocking(channel, buffer);
               if (res <= 0)
               {
                  return total;
               }
               total += (long) res;
               remainingBytes -= res;
            }
         }
         finally
         {
            buffer.position(0).limit(0);
         }
      }
      return total;
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
      final SocketChannel channel = this.channel.getChannel();
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
