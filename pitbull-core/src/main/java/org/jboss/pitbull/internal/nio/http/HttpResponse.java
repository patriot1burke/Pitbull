package org.jboss.pitbull.internal.nio.http;

import org.jboss.pitbull.spi.OrderedHeaders;
import org.jboss.pitbull.spi.RequestHeader;
import org.jboss.pitbull.spi.ResponseHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HttpResponse
{
   protected int status;
   protected String statusMessage;
   protected OrderedHeaders headers;

   public HttpResponse(int status, String statusMessage)
   {
      this.status = status;
      this.statusMessage = statusMessage;
   }

   public HttpResponse(ResponseHeader response)
   {
      this.status = response.getStatus();
      this.statusMessage = response.getStatusMessage();
      this.headers = response.getHeaders();
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

   /**
    * Depending on response code and HTTP method an empty message body may have to be sent.  i.e. a Content-Length of 0
    *
    * @param request
    */
   public void prepareEmptyBody(RequestHeader request)
   {
      if (status < 200 || status == 204 || status == 304)
      {
         return;
      }
      if (request.getMethod().equalsIgnoreCase("HEAD")) return;
      getHeaders().removeHeader("Content-Length");
      getHeaders().removeHeader("Transfer-Encoding");
      getHeaders().addHeader("Content-Length", "0");
      return;
   }

   public byte[] responseBytes() throws IOException
   {
      String statusMessage = getStatusMessage();
      if (statusMessage == null)
      {
         HttpResponseStatus status = HttpResponseStatus.valueOf(getStatus());
         statusMessage = status.getReasonPhrase();
      }
      StringBuilder builder = new StringBuilder(100);
      builder.append("HTTP/1.1 ");
      builder.append(getStatus());
      builder.append(' ');
      builder.append(statusMessage);
      builder.append("\r\n");
      if (getHeaders() != null)
      {
         for (Map.Entry<String, String> entry : getHeaders().getHeaderList())
         {
            builder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
         }
      }
      builder.append("\r\n");


      return builder.toString().getBytes("UTF-8");
   }


}
