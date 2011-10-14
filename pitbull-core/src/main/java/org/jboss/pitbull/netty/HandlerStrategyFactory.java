package org.jboss.pitbull.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.pitbull.NotImplementedYetException;
import org.jboss.pitbull.spi.InputStreamHandler;
import org.jboss.pitbull.spi.OutputStreamHandler;
import org.jboss.pitbull.spi.RequestHandler;
import org.jboss.pitbull.spi.RequestHeader;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HandlerStrategyFactory
{
   public static class AggregatedStreamHandlerStrategy implements HandlerStrategy
   {
      protected RequestHandler handler;
      protected ChannelBuffer buffer;

      public AggregatedStreamHandlerStrategy(RequestHandler handler)
      {
         this.handler = handler;
      }

      @Override
      public void beginChunked()
      {
         buffer = ChannelBuffers.dynamicBuffer();
      }

      @Override
      public void addChunk(ChannelBuffer chunk)
      {
         buffer.writeBytes(chunk);
      }

      @Override
      public void lastChunk(ChannelBuffer chunk, List<Map.Entry<String, String>> trailHeaders, NettyRequestHeader requestHeader, Channel channel)
      {
         buffer.writeBytes(chunk);
         ChannelBufferInputStream is = new ChannelBufferInputStream(buffer);
         ((InputStreamHandler)handler).setInputStream(is);
         requestHeader.getHeaders().addAll(trailHeaders);
         execute(requestHeader, channel);

      }

      private void execute(NettyRequestHeader requestHeader, Channel channel)
      {
         boolean keepAlive = HttpHeaders.isKeepAlive(requestHeader.getRequest());
         NettyStreamResponseWriter writer = new NettyStreamResponseWriter(channel, keepAlive);
         ((OutputStreamHandler)handler).setWriter(writer);
         handler.execute(requestHeader);
      }

      @Override
      public void notChunked(NettyRequestHeader requestHeader, Channel channel)
      {
         ChannelBufferInputStream is = new ChannelBufferInputStream(requestHeader.getRequest().getContent());
         ((InputStreamHandler)handler).setInputStream(is);
         execute(requestHeader, channel);
      }

      @Override
      public void disconnected()
      {
         throw new NotImplementedYetException();
      }
   }

   public static HandlerStrategy create(RequestHandler handler)
   {
      if (handler instanceof OutputStreamHandler && handler instanceof InputStreamHandler)
      {
         return new AggregatedStreamHandlerStrategy(handler);
      }
      else
      {
         throw new NotImplementedYetException();
      }
   }
}
