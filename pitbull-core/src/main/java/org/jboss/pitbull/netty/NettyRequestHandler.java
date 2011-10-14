package org.jboss.pitbull.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpChunkTrailer;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;
import org.jboss.pitbull.util.registry.NotFoundException;
import org.jboss.pitbull.util.registry.UriRegistry;
import org.jboss.pitbull.spi.RequestHandler;
import org.jboss.pitbull.spi.RequestInitiator;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import java.util.List;
import java.util.Map.Entry;

import static org.jboss.netty.channel.Channels.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.*;

/**
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author Andy Taylor (andy.taylor@jboss.org)
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @version $Rev: 2368 $, $Date: 2010-10-18 17:19:03 +0900 (Mon, 18 Oct 2010) $
 */
public class NettyRequestHandler extends SimpleChannelUpstreamHandler
{

   private static final ChannelBuffer CONTINUE = ChannelBuffers.copiedBuffer(
           "HTTP/1.1 100 Continue\r\n\r\n", CharsetUtil.US_ASCII);

   private UriRegistry<RequestInitiator> registry;
   private SSLEngine ssl;

   private NettyRequestHeader requestHeader;
   private NettyConnection connection;
   private HandlerStrategy handlerStrategy;


   public NettyRequestHandler(UriRegistry<RequestInitiator> registry, SSLEngine ssl)
   {
      this.registry = registry;
      this.ssl = ssl;
   }

   @Override
   public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
           throws Exception
   {

      Object msg = e.getMessage();

      if (msg instanceof HttpRequest)
      {
         HttpRequest m = (HttpRequest) msg;

         Channel channel = ctx.getChannel();
         // Handle the 'Expect: 100-continue' header if necessary.
         // TODO: Respond with 413 Request Entity Too Large
         //   and discard the traffic or close the connection.
         //       No need to notify the upstream handlers - just log.
         //       If decoding a response, just throw an exception.
         if (is100ContinueExpected(m))
         {
            write(ctx, succeededFuture(channel), CONTINUE.duplicate());
         }

         SSLSession sslSession = null;
         if (ssl != null)
         {
            sslSession = ssl.getSession();
         }
         if (connection == null)
         {
            connection = new NettyConnection(channel.getId(), channel.getLocalAddress(), channel.getRemoteAddress(), sslSession, sslSession != null);
         }
         requestHeader = new NettyRequestHeader(m);

         try
         {
            List<RequestInitiator> initiators = registry.match(requestHeader.getUri());
            for (RequestInitiator initiator : initiators)
            {
               RequestHandler requestHandler = initiator.begin(connection, requestHeader);
               if (requestHandler == null) continue;
               handlerStrategy = HandlerStrategyFactory.create(requestHandler);
            }
            if (handlerStrategy == null)
            {
               notFound(ctx);
               return;
            }

         }
         catch (NotFoundException e1)
         {
            notFound(ctx);
            return;
         }

         if (m.isChunked())
         {
            handlerStrategy.beginChunked();
         }
         else
         {
            handlerStrategy.notChunked(requestHeader, ctx.getChannel());
         }
      }
      else if (msg instanceof HttpChunk)
      {
         // Sanity check
         if (requestHeader == null)
         {
            throw new IllegalStateException(
                    "received " + HttpChunk.class.getSimpleName() +
                            " without " + HttpMessage.class.getSimpleName());
         }

         // Merge the received chunk into the content of the current message.
         HttpChunk chunk = (HttpChunk) msg;

         if (chunk.isLast())
         {
            // Merge trailing headers into the message.
            List<Entry<String, String>> trailerHeaders = null;
            if (chunk instanceof HttpChunkTrailer)
            {
               HttpChunkTrailer trailer = (HttpChunkTrailer) chunk;
               trailerHeaders = trailer.getHeaders();
            }
            handlerStrategy.lastChunk(chunk.getContent(), trailerHeaders, requestHeader, ctx.getChannel());
            handlerStrategy = null;
         }
         else
         {
            handlerStrategy.addChunk(chunk.getContent());
         }
      }
      else
      {
         System.out.println("Message unknown:" + e.getMessage());
         // Neither HttpMessage or HttpChunk
         ctx.sendUpstream(e);
      }
   }

   protected void notFound(ChannelHandlerContext ctx)
   {
      HttpResponse errorResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
      ChannelFuture future = ctx.getChannel().write(errorResponse);
      if (!HttpHeaders.isKeepAlive(requestHeader.getRequest()))
      {
         future.addListener(ChannelFutureListener.CLOSE);
      }
   }
}
