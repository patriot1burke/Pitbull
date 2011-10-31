package org.jboss.pitbull.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.*;

/**
 * An input stream which reads from a http chunk stream source channel with a buffer.  In addition, the
 * {@link #available()} method can be used to determine whether the next read will or will not block.
 *
 * @apiviz.exclude
 * @since 2.1
 */
public class ChunkedInputStream extends ContentInputStream
{
   private final ManagedChannel channel;
   private final ByteBuffer buffer;
   private volatile boolean closed;
   private boolean done;
   private long remainingChunkBytes = 0;
   private boolean initial = true;

   /**
    * Construct a new instance.
    *
    * @param channel    the channel to wrap
    * @param bufferSize the size of the internal buffer
    */
   public ChunkedInputStream(final ManagedChannel channel, ByteBuffer buffer)
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

   public void eat() throws IOException
   {
      while (skip(1000) != 0);
   }

   protected long getChunkSize(String hex)
   {
      hex = hex.trim();
      for (int i = 0; i < hex.length(); i++)
      {
         char c = hex.charAt(i);
         if (c == ';' || Character.isWhitespace(c) || Character.isISOControl(c))
         {
            hex = hex.substring(0, i);
            break;
         }
      }

      return Long.parseLong(hex, 16);
   }


   protected long readSize() throws IOException
   {
      StringBuilder chunkSizeBuilder = new StringBuilder(10);

      READ_SIZE:
      for (; ; )
      {
         while (buffer.hasRemaining())
         {
            byte b = buffer.get();
            if (b == HttpRequestDecoder.LF)
            {
               // strip out \r
               if (chunkSizeBuilder.length() > 0 && chunkSizeBuilder.charAt(chunkSizeBuilder.length() - 1) == '\r')
                  chunkSizeBuilder.setLength(chunkSizeBuilder.length() - 1);
               String hex = chunkSizeBuilder.toString();
               if (initial)
               {
                  initial = false;
               }
               else if (hex.length() == 0)// chunk ends with \r\n
               {
                  chunkSizeBuilder = new StringBuilder(10);
                  continue;
               }
               long currentChunkSize = getChunkSize(hex);
               if (currentChunkSize > 0)
               {
                  return currentChunkSize;
               }
               break READ_SIZE;
            }
            else
            {
               chunkSizeBuilder.append((char) b);
            }
         }
         buffer.clear();
         if (readBuffer(buffer) == -1)
         {
            done = true;
            return -1;
         }
         buffer.flip();
      }
      StringBuilder lineBuilder = new StringBuilder(100);
      // we're at the trailer if we got this far.
      for (; ; )
      {
         while (buffer.hasRemaining())
         {
            byte b = buffer.get();
            if (b == HttpRequestDecoder.LF)
            {
               if (lineBuilder.length() > 0 && lineBuilder.charAt(lineBuilder.length() - 1) == '\r')
                  lineBuilder.setLength(lineBuilder.length() - 1);
               String line = lineBuilder.toString();
               if (line.length() == 0)
               {
                  done = true;
                  return -1;
               }
               lineBuilder = new StringBuilder(100);

            }
            else
            {
               lineBuilder.append((char) b);
            }
         }
         buffer.clear();
         if (readBuffer(buffer) == -1)
         {
            done = true;
            return -1;
         }
         buffer.flip();
      }
   }

   /**
    * Blocks until at least one byte is read or EOF.  Does not clear or flip the buffer.
    *
    * @param buf
    * @return
    * @throws IOException
    */
   protected int readBuffer(ByteBuffer buf) throws IOException
   {
      if (timeout == 0L)
      {
         for (;;)
         {
            final int res = Channels.readBlocking(channel.getChannel(), buf);
            if (res == -1)
            {
               return -1;
            }
            else if (res == 0)
            {
               continue;
            }
            return res;
         }
      }
      else
      {
         long now = System.currentTimeMillis();
         final long deadline = timeout - now;
         for(;;)
         {
            if (deadline <= now)
            {
               throw new ReadTimeoutException("Read timed out");
            }
            final int res = Channels.readBlocking(channel.getChannel(), buf, deadline - now, TimeUnit.MILLISECONDS);
            if (res == -1)
            {
               return -1;
            }
            else if (res == 0)
            {
               continue;
            }
            return res;
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
      if (closed || done) return -1;

      if (remainingChunkBytes <= 0)
      {
         long size = readSize();
         if (size < 1)
         {
            return -1;
         }
         remainingChunkBytes = size;
      }

      if (buffer.hasRemaining())
      {
         remainingChunkBytes--;
         return buffer.get() & 0xff;
      }

      buffer.clear();
      if (readBuffer(buffer) == -1)
      {
         done = true;
         return -1;
      }
      buffer.flip();

      remainingChunkBytes--;
      return buffer.get() & 0xFF;

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
      if (closed || done) return -1;

      if (len < 1)
      {
         return 0;
      }
      if (remainingChunkBytes <= 0)
      {
         long size = readSize();
         if (size < 1)
         {
            done = true;
            return -1;
         }
         remainingChunkBytes = size;
      }

      if (len > remainingChunkBytes)
      {
         len = (int) remainingChunkBytes;
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
         remainingChunkBytes -= cnt;
      }
      if (closed) return -1;
      if (len <= 0) return total;

      ByteBuffer buf = ByteBuffer.wrap(b, off, len);
      int read = readBuffer(buf);
      if (read == -1)
      {
         done = true;
         return -1;
      }
      total += read;
      remainingChunkBytes -= read;
      return total;

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
      long skipRemaining = n;
      while (skipRemaining > 0)
      {
         long skipped = skipChunk(n);
         if (skipped == 0L) return 0L;
         skipRemaining -= skipped;
      }
      return n;
   }

   protected long skipChunk(long n) throws IOException
   {
      if (closed || done) return 0L;
      if (n < 1L)
      {
         return 0L;
      }

      long total = 0;

      if (remainingChunkBytes <= 0)
      {
         long size = readSize();
         if (size < 1)
         {
            done = true;
            return 0L;
         }
         remainingChunkBytes = size;
      }

      if (n > remainingChunkBytes)
      {
         n = remainingChunkBytes;
      }

      if (buffer.hasRemaining())
      {
         final int cnt = (int) min(buffer.remaining(), n);
         ByteBuffers.skip(buffer, cnt);
         remainingChunkBytes -= cnt;
         n -= cnt;
         total += cnt;
      }

      if (n < 1) return total;

      buffer.clear();
      int read = readBuffer(buffer);
      if (read == -1)
      {
         done = true;
         return total;
      }
      buffer.flip();
      return total;
   }

   /**
    * Return the number of bytes available to read
    *
    * @return the number of ready bytes, or 0 for none
    * @throws java.io.IOException if an I/O error occurs
    */
   public int available() throws IOException
   {
      if (closed || done) return 0;
      if (remainingChunkBytes < 1) return 0;

      return (int)min(buffer.remaining(), remainingChunkBytes);
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
