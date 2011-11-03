package org.jboss.pitbull.nio.http;

import org.jboss.pitbull.NotImplementedYetException;
import org.jboss.pitbull.nio.socket.Channels;
import org.jboss.pitbull.nio.socket.ManagedChannel;
import org.jboss.pitbull.spi.ContentOutputStream;
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

   public NioStreamResponseWriter(ManagedChannel channel, boolean keepAlive)
   {
      this.channel = channel;
      this.keepAlive = keepAlive;
   }

   @Override
   public ContentOutputStream getStream(ResponseHeader responseHeader)
   {
      stream = new BufferedContentOutputStream(channel, responseHeader);
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
      if (stream == null)
      {
         HttpResponse response = new HttpResponse(responseHeader);
         try
         {
            byte[] bytes = response.responseBytes();
            Channels.writeBlocking(channel.getChannel(), ByteBuffer.wrap(bytes));
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
