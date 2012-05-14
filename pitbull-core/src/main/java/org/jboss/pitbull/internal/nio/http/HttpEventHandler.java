package org.jboss.pitbull.internal.nio.http;

import org.jboss.pitbull.Connection;
import org.jboss.pitbull.RequestHeader;
import org.jboss.pitbull.StatusCode;
import org.jboss.pitbull.internal.logging.Logger;
import org.jboss.pitbull.internal.nio.socket.EventHandler;
import org.jboss.pitbull.internal.nio.socket.ManagedChannel;
import org.jboss.pitbull.internal.nio.websocket.WebSocketEventHandler;
import org.jboss.pitbull.internal.util.registry.NotFoundException;
import org.jboss.pitbull.internal.util.registry.UriRegistry;
import org.jboss.pitbull.server.handlers.WebSocketHandler;
import org.jboss.pitbull.server.handlers.stream.StreamRequestHandler;
import org.jboss.pitbull.server.spi.RequestHandler;
import org.jboss.pitbull.server.spi.RequestInitiator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HttpEventHandler implements EventHandler
{
   public static final int BUFFER_SIZE = 8192;
   protected HttpRequestDecoder decoder;
   protected ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
   protected Connection connection;
   protected UriRegistry<Object> registry;
   protected ExecutorService executor;
   protected static final Logger log = Logger.getLogger(HttpEventHandler.class);
   protected long count;

   public HttpEventHandler(ExecutorService executor, UriRegistry<Object> registry)
   {
      this.executor = executor;
      this.registry = registry;
   }

   protected void error(ManagedChannel channel, StatusCode code, HttpRequestHeader requestHeader) throws IOException
   {
      log.trace("Error returning with code: {0}", code.toString());
      ContentInputStream is = ContentInputStream.create(channel, buffer, requestHeader.getHeaders());
      if (is != null) is.eat();
      log.trace("ate stream");
      HttpResponse response = new HttpResponse(code);
      byte[] bytes = response.responseBytes();
      log.trace("writing error");
      channel.writeBlocking(ByteBuffer.wrap(bytes));
      log.trace("wrote error");
   }

   @Override
   public void handleRead(ManagedChannel channel)
   {
      log.trace("handleRead() on channel {0}", channel.getId());
      try
      {
         if (buffer == null) buffer = ByteBuffer.allocate(BUFFER_SIZE);

         try
         {
            buffer.clear();
            int c = channel.read(buffer);
            if (c == -1)
            {
               channel.close();
            }
            else if (c == 0) return;
            buffer.flip();
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }

         if (decoder == null) decoder = new HttpRequestDecoder();
         if (connection == null)
         {
            connection = new ConnectionImpl(
                    channel.getChannel().socket().getLocalSocketAddress(),
                    channel.getChannel().socket().getRemoteSocketAddress(),
                    channel.getSslSession() != null);
         }

         log.trace("decode buffer");
         if (!decoder.process(buffer))
         {
            log.trace("Not enough to decode buffer");
            return;
         }

         log.trace("Http request decoded");

         HttpRequestHeader requestHeader = decoder.getRequest();
         log.trace("-- Http request: {0}", requestHeader);
         decoder = null;

         RequestInitiator initiator = null;
         RequestHandler requestHandler = null;
         try
         {
            List<Object> matches = registry.match(requestHeader.getUri());
            for (Object match : matches)
            {
               if (match instanceof RequestInitiator)
               {
                  initiator = (RequestInitiator) match;
                  requestHandler = initiator.begin(connection, requestHeader);
               }
               else if (match instanceof RequestHandler)
               {
                  requestHandler = (RequestHandler) match;
               }
               if (requestHandler != null) break;
            }
         }
         catch (NotFoundException e1)
         {
         }
         if (requestHandler == null)
         {
            log.trace("Could not find a requestHandler, returning 404: {0} ", requestHeader);
            try
            {
               error(channel, StatusCode.NOT_FOUND, requestHeader);
            }
            catch (Throwable e)
            {
               log.error("Failed to send error message to client, closing", e);
               channel.close();
            }
            return;
         }

         if (requestHandler instanceof StreamRequestHandler)
         {
            ByteBuffer oldBuffer = buffer;
            buffer = null;
            executeStreamRequestHandler(oldBuffer, channel, requestHeader, (StreamRequestHandler) requestHandler);
         }
         else if (requestHandler instanceof WebSocketHandler)
         {
            ByteBuffer oldBuffer = buffer;
            buffer = null;
            executeWebSocketHandler(oldBuffer, channel, requestHeader, (WebSocketHandler) requestHandler);

         }
         else
         {
            log.error("Unsupported requestHandler type: " + requestHandler.getClass().getName());
            if (initiator != null)
            {
               try
               { initiator.illegalHandler(requestHandler); }
               catch (Throwable ignored)
               {}
            }
            try
            {
               error(channel, StatusCode.INTERNAL_SERVER_ERROR, requestHeader);
            }
            catch (Throwable e)
            {
               log.error("Failed to send error message to client, closing", e);
               channel.close();
            }
            return;
         }

      }
      finally
      {
         log.trace("<--- Exit handleRead() channel: {0}", channel.getId());
      }
   }

   protected void executeWebSocketHandler(final ByteBuffer buffer, final ManagedChannel channel, final RequestHeader requestHeader, final WebSocketHandler requestHandler)
   {
      log.trace("Using WebSocketHandler channel: {0}", channel.getId());
      final WebSocketEventHandler webSocketEventHandler = new WebSocketEventHandler(connection, channel, executor, requestHandler);
      channel.setHandler(webSocketEventHandler);
      channel.suspendReads();

      executor.execute(new Runnable()
      {
         @Override
         public void run()
         {
            try
            {
               webSocketEventHandler.handshake(requestHeader, buffer);
               channel.resumeReads();
            }
            catch (Exception e)
            {
               channel.close();
            }
         }
      });
   }

   protected void executeStreamRequestHandler(ByteBuffer buffer, ManagedChannel channel, HttpRequestHeader requestHeader, StreamRequestHandler requestHandler)
   {
      log.trace("Using StreamHandler channel: {0}", channel.getId());
      channel.suspendReads();
      StreamRequestHandler streamHandler = (StreamRequestHandler) requestHandler;

      StreamExecutor task = new StreamExecutor(connection, channel, streamHandler, buffer, requestHeader);
      executor.execute(task);
   }

   @Override
   public void shutdown()
   {
   }
}
