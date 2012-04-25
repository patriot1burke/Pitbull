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
   protected ManagedChannel channel;
   protected ContentOutputStream stream;
   protected RequestHeader requestHeader;
   protected ContentInputStream is;
   protected boolean ended;
   protected static final Logger log = Logger.getLogger(NioStreamResponseWriter.class);

   public NioStreamResponseWriter(ManagedChannel channel, RequestHeader requestHeader, ContentInputStream is)
   {
      this.channel = channel;
      this.requestHeader = requestHeader;
      this.is = is;
   }

   @Override
   public boolean isEnded()
   {
      return ended;
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
      if (ended)
      {
         log.error("StreamResponseWriter.end() called twice");
         return;
      }
      ended = true;
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
         // stream was closed
         HttpResponse response = new HttpResponse(responseHeader);
         try
         {
            response.prepareEmptyBody(requestHeader);
            byte[] bytes = response.responseBytes();
            channel.writeBlocking(ByteBuffer.wrap(bytes));
            return;
         }
         catch (IOException e)
         {
            throw new NotImplementedYetException(e);

         }
      }
      else
      {
         // stream was open
         try
         {
            stream.close();
         }
         catch (IOException e)
         {
            throw new NotImplementedYetException(e);
         }

      }


   }
}
