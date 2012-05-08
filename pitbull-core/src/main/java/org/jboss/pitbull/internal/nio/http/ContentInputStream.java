package org.jboss.pitbull.internal.nio.http;

import org.jboss.pitbull.OrderedHeaders;
import org.jboss.pitbull.handlers.PitbullChannel;
import org.jboss.pitbull.internal.nio.socket.ManagedChannel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class ContentInputStream extends InputStream
{
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

   /**
    * Eat the entity body of the HTTP message
    */
   public abstract void eat() throws IOException;

   public static ContentInputStream create(PitbullChannel channel, ByteBuffer initialBuffer, OrderedHeaders headers)
   {
      String cl = headers.getFirstHeader("Content-Length");
      if (cl != null)
      {
         long contentLength = Long.parseLong(cl);
         return new ContentLengthInputStream(channel, initialBuffer, contentLength);
      }
      String transferEncoding = headers.getFirstHeader("Transfer-Encoding");
      if (transferEncoding != null)
      {
         return new ChunkedInputStream(channel, initialBuffer);
      }
      return new NullInputStream();
   }
}
