package org.jboss.pitbull.internal.nio.http;

import org.jboss.pitbull.internal.NotImplementedYetException;
import org.jboss.pitbull.internal.logging.Logger;
import org.jboss.pitbull.internal.nio.socket.ManagedChannel;
import org.jboss.pitbull.spi.ContentOutputStream;
import org.jboss.pitbull.spi.OrderedHeaders;
import org.jboss.pitbull.spi.RequestHeader;
import org.jboss.pitbull.spi.StatusCode;
import org.jboss.pitbull.spi.StreamedResponse;
import org.jboss.pitbull.util.OrderedHeadersImpl;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class NioStreamedResponse implements StreamedResponse
{
   protected StatusCode status = StatusCode.INTERNAL_SERVER_ERROR;
   protected OrderedHeaders headers = new OrderedHeadersImpl();
   protected ManagedChannel channel;
   protected ContentOutputStream stream;
   protected RequestHeader requestHeader;
   protected ContentInputStream is;
   protected boolean ended;
   protected static final Logger log = Logger.getLogger(NioStreamedResponse.class);

   public NioStreamedResponse(ManagedChannel channel, RequestHeader requestHeader, ContentInputStream is)
   {
      this.channel = channel;
      this.requestHeader = requestHeader;
      this.is = is;
   }

   @Override
   public StatusCode getStatusCode()
   {
      return status;
   }

   @Override
   public void setStatus(StatusCode status)
   {
      this.status = status;
   }

   @Override
   public OrderedHeaders getHeaders()
   {
      return headers;
   }

   @Override
   public boolean isCommitted()
   {
      if (stream == null) return false;
      return stream.isCommitted();
   }

   @Override
   public boolean isEnded()
   {
      return ended;
   }

   @Override
   public void reset()
   {
      if (isEnded() || isCommitted()) throw new IllegalStateException("Response is committed");
      if (stream != null)
      {
         stream.reset();
      }
   }

   @Override
   public ContentOutputStream getOutputStream()
   {
      if (stream == null) stream = new BufferedContentOutputStream(channel, requestHeader, this);
      return stream;
   }

   @Override
   public void end()
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
         HttpResponse response = new HttpResponse(this);
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
