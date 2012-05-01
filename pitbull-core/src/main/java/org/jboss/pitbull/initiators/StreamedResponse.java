package org.jboss.pitbull.initiators;

import org.jboss.pitbull.spi.ContentOutputStream;
import org.jboss.pitbull.spi.OrderedHeaders;
import org.jboss.pitbull.spi.ResponseHeader;
import org.jboss.pitbull.spi.StatusCode;
import org.jboss.pitbull.spi.StreamResponseWriter;
import org.jboss.pitbull.util.OrderedHeadersImpl;

import java.io.OutputStream;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class StreamedResponse
{
   protected StatusCode status;
   protected OrderedHeaders headers = new OrderedHeadersImpl();
   protected StreamResponseWriter writer;
   protected ResponseHeader delegator = new ResponseHeader()
   {
      @Override
      public StatusCode getStatusCode()
      {
         return status;
      }

      @Override
      public OrderedHeaders getHeaders()
      {
         return headers;
      }
   };

   public StreamedResponse(StreamResponseWriter writer)
   {
      this.writer = writer;
   }

   public void setStatus(StatusCode status)
   {
      this.status = status;
   }

   public void setHeaders(OrderedHeaders headers)
   {
      this.headers = headers;
   }

   public ContentOutputStream getStream()
   {
      return writer.getStream(delegator);
   }

   public StatusCode getStatus()
   {
      return status;
   }

   public OrderedHeaders getHeaders()
   {
      return headers;
   }
}
