package org.jboss.pitbull.client.internal;

import org.jboss.pitbull.OrderedHeaders;
import org.jboss.pitbull.RequestHeader;
import org.jboss.pitbull.handlers.PitbullChannel;
import org.jboss.pitbull.internal.nio.http.BufferedContentOutputStream;
import org.jboss.pitbull.internal.nio.http.HttpRequestHeader;

import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ClientContentOutputStream extends BufferedContentOutputStream
{
   protected static class ClientContentMessage implements ContentMessage
   {
      protected HttpRequestHeader requestHeader;

      public ClientContentMessage(HttpRequestHeader requestHeader)
      {
         this.requestHeader = requestHeader;
      }

      @Override
      public OrderedHeaders getHeaders()
      {
         return requestHeader.getHeaders();
      }

      @Override
      public byte[] getMessageBytes() throws IOException
      {
         StringBuilder builder = new StringBuilder(100);
         builder.append(requestHeader.getMethod());
         builder.append(' ');
         builder.append(requestHeader.getUri());
         builder.append(' ');
         builder.append(requestHeader.getHttpVersion());
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

      @Override
      public void prepareEmptyBody()
      {
         getHeaders().removeHeader("Content-Length");
         getHeaders().removeHeader("Transfer-Encoding");
      }
   }

   public ClientContentOutputStream(HttpRequestHeader requestHeader, PitbullChannel channel, int bufferSize)
   {
      super(channel, bufferSize);
      contentMessage = new ClientContentMessage(requestHeader);
   }
}
