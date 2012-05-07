package org.jboss.pitbull.internal.nio.websocket;

import org.jboss.pitbull.Connection;
import org.jboss.pitbull.OrderedHeaders;
import org.jboss.pitbull.RequestHeader;
import org.jboss.pitbull.ResponseHeader;
import org.jboss.pitbull.StatusCode;
import org.jboss.pitbull.handlers.websocket.WebSocket;
import org.jboss.pitbull.handlers.websocket.WebSocketHandler;
import org.jboss.pitbull.internal.nio.socket.BufferedBlockingInputStream;
import org.jboss.pitbull.internal.nio.socket.EventHandler;
import org.jboss.pitbull.internal.nio.socket.ManagedChannel;
import org.jboss.pitbull.util.OrderedHeadersImpl;
import org.jboss.pitbull.internal.nio.websocket.impl.oio.ClosingStrategy;
import org.jboss.pitbull.internal.nio.websocket.impl.oio.HttpRequestBridge;
import org.jboss.pitbull.internal.nio.websocket.impl.oio.HttpResponseBridge;
import org.jboss.pitbull.internal.nio.websocket.impl.oio.OioWebSocket;
import org.jboss.pitbull.internal.nio.websocket.impl.oio.WebSocketConnectionManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class WebSocketEventHandler implements EventHandler
{
   protected WebSocketHandler handler;
   protected WebSocket webSocket;
   protected ExecutorService executor;
   protected Connection connection;
   protected ManagedChannel channel;

   public WebSocketEventHandler(Connection connection, ManagedChannel channel, ExecutorService executor)
   {
      this.connection = connection;
      this.channel = channel;
      this.executor = executor;
   }

   public void init(final WebSocketHandler handler, final RequestHeader requestHeader, final ByteBuffer leftOverBuffer) throws Exception
   {
      final BufferedBlockingInputStream is = new BufferedBlockingInputStream(channel, leftOverBuffer);
      HttpRequestBridge requestBridge = new HttpRequestBridge()
      {
         @Override
         public String getHeader(String name)
         {
            return requestHeader.getHeaders().getFirstHeader(name);
         }

         @Override
         public String getRequestURI()
         {
            return requestHeader.getUri();
         }

         @Override
         public InputStream getInputStream()
         {
            return is;
         }
      };

      final ResponseHeader responseHeader = new ResponseHeader()
      {
         OrderedHeaders headers = new OrderedHeadersImpl();
         @Override
         public StatusCode getStatusCode()
         {
            return StatusCode.SWITCHING_PROTOCOLS;
         }

         @Override
         public OrderedHeaders getHeaders()
         {
            return headers;
         }
      };
      final HandshakeOutputStream os = new HandshakeOutputStream(responseHeader, channel, 8192);
      HttpResponseBridge responseBridge = new HttpResponseBridge()
      {
         @Override
         public String getHeader(String name)
         {
            return responseHeader.getHeaders().getFirstHeader(name);
         }

         @Override
         public void setHeader(String name, String val)
         {
            responseHeader.getHeaders().setHeader(name, val);
         }

         @Override
         public OutputStream getOutputStream()
         {
            return os;
         }

         @Override
         public void startUpgrade()
         {
         }

         @Override
         public void sendUpgrade() throws IOException
         {
            os.flush();
         }
      };

      ClosingStrategy closingStrategy = new ClosingStrategy()
      {
         @Override
         public void doClose() throws IOException
         {
            channel.close();
         }
      };

      OioWebSocket oioWebSocket = WebSocketConnectionManager.establish(handler.getProtocolName(), requestBridge, responseBridge, closingStrategy);
      webSocket = new WebSocketImpl(connection, oioWebSocket, requestHeader);
   }

   @Override
   public void handleRead(ManagedChannel channel)
   {
      try
      {
         handler.onReceivedFrame(webSocket);
      }
      catch (Exception e)
      {
         channel.close();
      }
   }

   @Override
   public void shutdown()
   {
   }
}
