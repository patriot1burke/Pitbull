package org.jboss.pitbull.internal.nio.http;

import org.jboss.pitbull.OrderedHeaders;
import org.jboss.pitbull.RequestHeader;
import org.jboss.pitbull.ResponseHeader;
import org.jboss.pitbull.handlers.PitbullChannel;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
 * Works the same as BufferedOutputStream except it invokes a callback prior to:
 * - initial flush of buffer
 * - subsequent flush of buffer
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ServerContentOutputStream extends BufferedContentOutputStream
{
   public static class ServerContentMessage implements ContentMessage
   {
      protected RequestHeader requestHeader;
      protected ResponseHeader responseHeader;
      protected HttpResponse response;

      public ServerContentMessage(RequestHeader requestHeader, ResponseHeader responseHeader)
      {
         this.requestHeader = requestHeader;
         this.responseHeader = responseHeader;
         response = new HttpResponse(responseHeader);
      }

      @Override
      public OrderedHeaders getHeaders()
      {
         return responseHeader.getHeaders();
      }

      @Override
      public byte[] getMessageBytes() throws IOException
      {
         HttpResponse response = new HttpResponse(responseHeader);
         return response.responseBytes();
      }

      @Override
      public void prepareEmptyBody()
      {
         response.prepareEmptyBody(requestHeader);
      }
   }


   /**
    * delegate OutputStream can be null and set at another time (i.e. at initialFlush time)
    *
    * @param out
    */
   public ServerContentOutputStream(PitbullChannel channel, RequestHeader requestHeader, ResponseHeader responseHeader)
   {
      this(channel, requestHeader, responseHeader, 8192);
   }

   /**
    * delegate OutputStream can be null and set at another time (i.e. at initialFlush time)
    *
    * @param out
    * @param size must be > 0
    */
   public ServerContentOutputStream(PitbullChannel channel, RequestHeader requestHeader, ResponseHeader responseHeader, int size)
   {
      super(channel, size);
      this.contentMessage = new ServerContentMessage(requestHeader, responseHeader);
   }


}

