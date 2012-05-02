package org.jboss.pitbull.internal.nio.http;

import org.jboss.pitbull.OrderedHeaders;
import org.jboss.pitbull.RequestHeader;
import org.jboss.pitbull.StatusCode;
import org.jboss.pitbull.handlers.stream.ContentOutputStream;
import org.jboss.pitbull.handlers.stream.StreamedResponse;
import org.jboss.pitbull.internal.logging.Logger;
import org.jboss.pitbull.internal.nio.socket.ManagedChannel;
import org.jboss.pitbull.util.OrderedHeadersImpl;

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
   protected boolean detached;
   protected static final Logger log = Logger.getLogger(NioStreamedResponse.class);

   public NioStreamedResponse(ManagedChannel channel, RequestHeader requestHeader, ContentInputStream is)
   {
      this.channel = channel;
      this.requestHeader = requestHeader;
      this.is = is;
   }

   @Override
   public boolean isDetached()
   {
      return detached;
   }

   @Override
   public void detach()
   {
      this.detached = true;
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
      headers.clear();
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

   protected void resumeWorker()
   {
      // todo keep-alive logic, right now everything kept alive
      try
      {
         channel.resumeReads();
      }
      catch (Exception ex)
      {
         log.error("Failed to resume worker", ex);
         closeChannel();
         return;
      }
   }

   protected void closeChannel()
   {
      try
      { channel.close(); }
      catch (Throwable t)
      {}
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

      boolean mustClose = false;

      try
      {
         is.eat();  // eat the input stream
      }
      catch (Throwable e)
      {
         log.warn("Exception while eating", e);
         mustClose = true;
      }


      if (stream == null)
      {
         HttpResponse response = new HttpResponse(this);
         try
         {
            response.prepareEmptyBody(requestHeader);
            byte[] bytes = response.responseBytes();
            channel.writeBlocking(ByteBuffer.wrap(bytes));
         }
         catch (Throwable e)
         {
            log.error("Failed writing response", e);
            mustClose = true;
         }
      }
      else
      {
         // stream was open
         try
         {
            stream.close();
         }
         catch (Throwable e)
         {
            log.error("Failed writing response", e);
            mustClose = true;
         }
      }

      if (mustClose)
      {
         closeChannel();
      }
      else
      {
         resumeWorker();
      }
   }
}
