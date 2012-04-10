package org.jboss.pitbull.internal.nio.http;

import org.jboss.pitbull.NotImplementedYetException;
import org.jboss.pitbull.internal.logging.Logger;
import org.jboss.pitbull.internal.nio.socket.ManagedChannel;
import org.jboss.pitbull.spi.ContentOutputStream;
import org.jboss.pitbull.spi.RequestHeader;
import org.jboss.pitbull.spi.ResponseHeader;
import org.jboss.pitbull.spi.StreamResponseWriter;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class NioStreamResponseWriter implements StreamResponseWriter
{
   private boolean keepAlive;
   private ManagedChannel channel;
   private ContentOutputStream stream;
   private RequestHeader requestHeader;
   private ContentInputStream is;
   protected static final Logger log = Logger.getLogger(NioStreamResponseWriter.class);

   public NioStreamResponseWriter(ManagedChannel channel, RequestHeader requestHeader, ContentInputStream is, boolean keepAlive)
   {
      this.channel = channel;
      this.requestHeader = requestHeader;
      this.keepAlive = keepAlive;
      this.is = is;
   }

   @Override
   public ContentOutputStream getStream(ResponseHeader responseHeader)
   {
      stream = new BufferedContentOutputStream(channel, requestHeader, responseHeader);
      return stream;
   }

   @Override
   public ContentOutputStream getAllocatedStream()
   {
      return stream;
   }

   @Override
   public void end(ResponseHeader responseHeader)
   {
      try
      {
         is.eat();  // eat the input stream
      }
      catch (IOException e)
      {
         log.warn("Exception while eating", e);
      }
      if (stream == null)
      {
         HttpResponse response = new HttpResponse(responseHeader);
         try
         {
            response.prepareEmptyBody(requestHeader);
            byte[] bytes = response.responseBytes();
            channel.writeBlocking(ByteBuffer.wrap(bytes));
            if (keepAlive)
            {
               channel.resumeReads();
            }
            else
            {
               channel.close();
            }
            return;
         }
         catch (IOException e)
         {
            throw new NotImplementedYetException(e);

         }
      }

      // stream was open

      try
      {
         stream.close();
         if (keepAlive)
         {
            channel.resumeReads();
         }
         else
         {
            channel.close();
         }
      }
      catch (IOException e)
      {
         throw new NotImplementedYetException(e);
      }

   }
}
