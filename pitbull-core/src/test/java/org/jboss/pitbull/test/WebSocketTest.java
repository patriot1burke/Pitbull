package org.jboss.pitbull.test;

import org.jboss.pitbull.client.WebSocketBuilder;
import org.jboss.pitbull.server.HttpServer;
import org.jboss.pitbull.server.HttpServerBuilder;
import org.jboss.pitbull.server.handlers.WebSocketHandler;
import org.jboss.pitbull.websocket.TextFrame;
import org.jboss.pitbull.websocket.WebSocket;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class WebSocketTest
{
   public static HttpServer http;

   @BeforeClass
   public static void startup() throws Exception
   {
      http = new HttpServerBuilder().connector().add()
              .workers(1)
              .maxRequestThreads(1).build();
      http.start();
   }

   @AfterClass
   public static void shutdown() throws Exception
   {
      http.stop();
   }

   public static class Handler implements WebSocketHandler
   {
      @Override
      public String getProtocolName()
      {
         return null;
      }

      @Override
      public void onReceivedFrame(WebSocket socket) throws IOException
      {
         TextFrame frame = (TextFrame)socket.readFrame();
         System.out.println("Received: " + frame.getText());

         socket.writeTextFrame(frame.getText());
      }
   }

   @Test
   public void testWebSocket() throws Exception
   {
      Handler handler = new Handler();
      http.register("/websocket", handler);
      try
      {
         WebSocket socket = WebSocketBuilder.create().connect("ws://localhost:8080/websocket");
         socket.writeTextFrame("hello world");
         TextFrame frame = (TextFrame)socket.readFrame();
         Assert.assertEquals("hello world", frame.getText());
      }
      finally
      {
         http.unregister(handler);
      }
   }
}
