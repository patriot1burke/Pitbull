package org.jboss.pitbull.nio;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

/**
 * A utility class containing static methods to support channel usage.
 *
 * @apiviz.exclude
 */
public final class Channels
{

   /**
    * Simple utility method to execute a blocking write on a byte channel.  The method blocks until the bytes in the
    * buffer have been fully written.
    *
    * @param channel the channel to write on
    * @param buffer  the data to write
    * @param <C>     the channel type
    * @return the number of bytes written
    * @throws java.io.IOException if an I/O exception occurs
    * @since 1.2
    */
   public static int writeBlocking(SocketChannel channel, ByteBuffer buffer) throws IOException
   {
      int t = 0;
      while (buffer.hasRemaining())
      {
         final int res = channel.write(buffer);
         if (res == 0)
         {
            SelectorUtil.awaitWritable(channel);
         }
         else
         {
            t += res;
         }
      }
      return t;
   }

   /**
    * Simple utility method to execute a blocking write on a byte channel with a timeout.  The method blocks until
    * either the bytes in the buffer have been fully written, or the timeout expires, whichever comes first.
    *
    * @param channel the channel to write on
    * @param buffer  the data to write
    * @param time    the amount of time to wait
    * @param unit    the unit of time to wait
    * @param <C>     the channel type
    * @return the number of bytes written
    * @throws java.io.IOException if an I/O exception occurs
    * @since 1.2
    */
   public static int writeBlocking(SocketChannel channel, ByteBuffer buffer, long time, TimeUnit unit) throws IOException
   {
      long remaining = unit.toMillis(time);
      long now = System.currentTimeMillis();
      int t = 0;
      while (buffer.hasRemaining() && remaining > 0L)
      {
         int res = channel.write(buffer);
         if (res == 0)
         {
            SelectorUtil.awaitWritable(channel, remaining, TimeUnit.MILLISECONDS);
            remaining -= Math.max(-now + (now = System.currentTimeMillis()), 0L);
         }
         else
         {
            t += res;
         }
      }
      return t;
   }

   /**
    * Simple utility method to execute a blocking read on a readable byte channel.  This method blocks until the
    * channel is readable, and then the message is read.
    *
    * @param channel the channel to read from
    * @param buffer  the buffer into which bytes are to be transferred
    * @param <C>     the channel type
    * @return the number of bytes read
    * @throws java.io.IOException if an I/O exception occurs
    * @since 1.2
    */
   public static int readBlocking(SocketChannel channel, ByteBuffer buffer) throws IOException
   {
      int res;
      while ((res = channel.read(buffer)) == 0 && buffer.hasRemaining())
      {
         SelectorUtil.awaitReadable(channel);
      }
      return res;
   }

   /**
    * Simple utility method to execute a blocking read on a readable byte channel with a timeout.  This method blocks until the
    * channel is readable, and then the message is read.
    *
    * @param channel the channel to read from
    * @param buffer  the buffer into which bytes are to be transferred
    * @param time    the amount of time to wait
    * @param unit    the unit of time to wait
    * @param <C>     the channel type
    * @return the number of bytes read
    * @throws java.io.IOException if an I/O exception occurs
    * @since 1.2
    */
   public static int readBlocking(SocketChannel channel, ByteBuffer buffer, long time, TimeUnit unit) throws IOException
   {
      int res = channel.read(buffer);
      if (res == 0 && buffer.hasRemaining())
      {
         SelectorUtil.awaitReadable(channel, time, unit);
         return channel.read(buffer);
      }
      else
      {
         return res;
      }
   }

}
