package org.jboss.pitbull.netty;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.pitbull.NotImplementedYetException;
import org.jboss.pitbull.spi.ContentOutputStream;
import org.jboss.pitbull.spi.ResponseHeader;
import org.jboss.pitbull.spi.StreamResponseWriter;

import java.io.IOException;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class NettyStreamResponseWriter implements StreamResponseWriter
{
   private boolean keepAlive;
   private Channel channel;
   private NettyContentOutputStream stream;

   public NettyStreamResponseWriter(Channel channel, boolean keepAlive)
   {
      this.channel = channel;
      this.keepAlive = keepAlive;
   }

   public static HttpResponse createResponse(ResponseHeader responseHeader)
   {
      HttpResponseStatus httpResponseStatus;
      if (responseHeader.getStatusMessage() != null)
         httpResponseStatus = new HttpResponseStatus(responseHeader.getStatus(), responseHeader.getStatusMessage());
      else httpResponseStatus = HttpResponseStatus.valueOf(responseHeader.getStatus());
      HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus);
      response.getHeaders().addAll(responseHeader.getHeaders());
      return response;
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
         HttpResponse response = createResponse(responseHeader);
         ChannelFuture future = channel.write(response);
         // Close the non-keep-alive connection after the write operation is done.
         if (!keepAlive)
         {
            future.addListener(ChannelFutureListener.CLOSE);
         }
         return;
      }

      // stream was open

      try
      {
         stream.close();
      }
      catch (IOException e)
      {
         throw new NotImplementedYetException(e);
      }
      if (!keepAlive)
      {
         channel.close();
      }

   }
}
