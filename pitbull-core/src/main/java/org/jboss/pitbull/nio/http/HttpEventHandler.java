package org.jboss.pitbull.nio.http;

import org.jboss.pitbull.ConnectionImpl;
import org.jboss.pitbull.logging.Logger;
import org.jboss.pitbull.nio.socket.Channels;
import org.jboss.pitbull.nio.socket.EventHandler;
import org.jboss.pitbull.nio.socket.ManagedChannel;
import org.jboss.pitbull.spi.Connection;
import org.jboss.pitbull.spi.RequestHandler;
import org.jboss.pitbull.spi.RequestInitiator;
import org.jboss.pitbull.spi.StreamHandler;
import org.jboss.pitbull.util.registry.NotFoundException;
import org.jboss.pitbull.util.registry.UriRegistry;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
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
   protected UriRegistry<RequestInitiator> registry;
   protected ExecutorService executor;
   protected static final Logger log = Logger.getLogger(HttpEventHandler.class);
   protected long count;

   public HttpEventHandler(ExecutorService executor, UriRegistry<RequestInitiator> registry)
   {
      this.executor = executor;
      this.registry = registry;
   }

   protected void error(ManagedChannel channel, int code, HttpRequestHeader requestHeader) throws IOException
   {
      log.trace("Error returning with code: {0}", code);
      ContentInputStream is = ContentInputStream.create(channel, buffer, requestHeader);
      if (is != null) is.eat();
      log.trace("ate stream");
      HttpResponse response = new HttpResponse(code, null);
      byte[] bytes = response.responseBytes();
      log.trace("writing error");
      channel.writeBlocking(ByteBuffer.wrap(bytes));
      log.trace("wrote error");
   }

   @Override
   public void handleRead(ManagedChannel channel)
   {
      log.trace("handleRead()");
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
                    channel.getSslSession(),
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

         RequestHandler requestHandler = null;
         try
         {
            List<RequestInitiator> initiators = registry.match(requestHeader.getUri());
            for (RequestInitiator initiator : initiators)
            {
               requestHandler = initiator.begin(connection, requestHeader);
               if (requestHandler != null) break;
            }
         }
         catch (NotFoundException e1)
         {
         }
         channel.suspendReads();
         log.trace("suspended reads");
         if (requestHandler == null)
         {
            log.trace("requestHandler was null, returning 404");
            try
            {
               error(channel, 404, requestHeader);
            }
            catch (IOException e)
            {
               log.error("Failed to send error message to client, closing", e);
               channel.close();
            }
            channel.resumeReads();
            return;
         }


         if (!(requestHandler instanceof StreamHandler))
         {
            log.error("Unsupported requestHandler type: " + requestHandler.getClass().getName());
            requestHandler.unsupportedHandler();
            try
            {
               error(channel, 500, requestHeader);
            }
            catch (IOException e)
            {
               log.error("Failed to send error message to client, closing", e);
               channel.close();
            }
            return;
         }

         log.trace("Using StreamHandler");
         StreamHandler streamHandler = (StreamHandler) requestHandler;

         ByteBuffer oldBuffer = buffer;
         buffer = null;

         StreamExecutor task = new StreamExecutor(channel, streamHandler, oldBuffer, requestHeader);

         if (requestHandler.isFast())
         {
            task.run();
         }
         else
         {
            executor.execute(task);
         }
      }
      finally
      {
         log.trace("<--- Exit handleRead()");
      }
   }

   @Override
   public void shutdown()
   {
   }
}
