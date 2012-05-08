package org.jboss.pitbull.internal.nio.http;

import org.jboss.pitbull.OrderedHeaders;
import org.jboss.pitbull.RequestHeader;
import org.jboss.pitbull.ResponseHeader;
import org.jboss.pitbull.StatusCode;

import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HttpResponse
{
   protected StatusCode status;
   protected ResponseHeader responseHeader;

   public HttpResponse(StatusCode status)
   {
      this.status = status;
   }

   public HttpResponse(ResponseHeader response)
   {
      this.responseHeader = response;
   }

   public StatusCode getStatus()
   {
      if (responseHeader == null) return status;
      return responseHeader.getStatusCode();
   }

   public OrderedHeaders getHeaders()
   {
      if (responseHeader == null) return null;
      return responseHeader.getHeaders();
   }

   /**
    * Depending on response code and HTTP method an empty message body may have to be sent.  i.e. a Content-Length of 0
    *
    * @param request
    */
   public void prepareEmptyBody(RequestHeader request)
   {
      if (getStatus().getCode() < 200 || getStatus().getCode() == 204 || getStatus().getCode() == 304)
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
      StringBuilder builder = new StringBuilder(100);
      builder.append("HTTP/1.1 ");
      builder.append(getStatus().getCode());
      builder.append(' ');
      builder.append(getStatus().getStatusMessage());
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
