package org.jboss.pitbull.initiators;

import org.jboss.pitbull.spi.ContentOutputStream;
import org.jboss.pitbull.spi.OrderedHeaders;
import org.jboss.pitbull.spi.ResponseHeader;
import org.jboss.pitbull.spi.StreamResponseWriter;
import org.jboss.pitbull.util.OrderedHeadersImpl;

import java.io.OutputStream;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class StreamedResponse
{
   protected int status;
   protected String statusMessage;
   protected OrderedHeaders headers = new OrderedHeadersImpl();
   protected StreamResponseWriter writer;
   protected ResponseHeader delegator = new ResponseHeader()
   {
      @Override
      public int getStatus()
      {
         return status;
      }

      @Override
      public String getStatusMessage()
      {
         return statusMessage;
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

   public void setStatus(int status)
   {
      this.status = status;
   }

   public void setStatusMessage(String statusMessage)
   {
      this.statusMessage = statusMessage;
   }

   public void setHeaders(OrderedHeaders headers)
   {
      this.headers = headers;
   }

   public ContentOutputStream getStream()
   {
      return writer.getStream(delegator);
   }

   public int getStatus()
   {
      return status;
   }

   public String getStatusMessage()
   {
      return statusMessage;
   }

   public OrderedHeaders getHeaders()
   {
      return headers;
   }
}
