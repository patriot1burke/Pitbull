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
   protected SSLEngine ssl;
   protected ExecutorService executor;
   protected static final Logger log = Logger.getLogger(HttpEventHandler.class);
   protected long count;

   public HttpEventHandler(ExecutorService executor, SSLEngine ssl, UriRegistry<RequestInitiator> registry)
   {
      this.executor = executor;
      this.ssl = ssl;
      this.registry = registry;
   }

   protected void error(ManagedChannel channel, int code, HttpRequestHeader requestHeader) throws IOException
   {
      ContentInputStream is = ContentInputStream.create(channel, buffer, requestHeader);
      if (is != null) is.eat();
      HttpResponse response = new HttpResponse(code, null);
      byte[] bytes = response.responseBytes();
      channel.writeBlocking(ByteBuffer.wrap(bytes));
   }

   @Override
   public void handleRead(ManagedChannel channel)
   {
      //SocketChannel channel = managedChannel.getChannel();
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
         SSLSession sslSession = null;
         if (ssl != null)
         {
            sslSession = ssl.getSession();
         }
         connection = new ConnectionImpl(channel.getChannel().socket().getLocalSocketAddress(), channel.getChannel().socket().getRemoteSocketAddress(), sslSession, sslSession != null);
      }

      if (!decoder.process(buffer))
      {
         return;
      }

      HttpRequestHeader requestHeader = decoder.getRequest();
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

      if (requestHandler == null)
      {
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

   @Override
   public void shutdown()
   {
   }
}
