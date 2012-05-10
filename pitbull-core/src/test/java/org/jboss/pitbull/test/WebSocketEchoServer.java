package org.jboss.pitbull.test;

import org.jboss.pitbull.Connection;
import org.jboss.pitbull.RequestHeader;
import org.jboss.pitbull.StatusCode;
import org.jboss.pitbull.server.HttpServer;
import org.jboss.pitbull.server.HttpServerBuilder;
import org.jboss.pitbull.server.handlers.WebSocketHandler;
import org.jboss.pitbull.server.handlers.stream.StreamRequestHandler;
import org.jboss.pitbull.server.handlers.stream.StreamedResponse;
import org.jboss.pitbull.websocket.TextFrame;
import org.jboss.pitbull.websocket.WebSocket;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class WebSocketEchoServer
{
   public static class TextHandler implements WebSocketHandler
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
         System.out.println("WebSocketVersion: " + socket.getVersion());
         System.out.println("Received: " + frame.getText());
         try
         {
            Thread.sleep(10); // sleep so reads can buffer up.
         }
         catch (InterruptedException e)
         {
            throw new RuntimeException(e);
         }
         socket.writeTextFrame(frame.getText());
      }
   }

   public static class EchoHtml implements StreamRequestHandler
   {
      @Override
      public void execute(Connection connection, RequestHeader requestHeader, InputStream requestStream, StreamedResponse response) throws IOException
      {
         response.setStatus(StatusCode.OK);
         response.getHeaders().addHeader("Content-Type", "text/html");
         String host = requestHeader.getHeaders().getFirstHeader("Host");

         String page = "<!DOCTYPE HTML>\n" +
                 "<html>\n" +
                 "<head>\n" +
                 "<script type=\"text/javascript\">\n" +
                 "function WebSocketTest()\n" +
                 "{\n" +
                 "  if (\"WebSocket\" in window)\n" +
                 "  {\n" +
                 "     alert(\"WebSocket is supported by your Browser!\");\n" +
                 "     // Let us open a web socket\n" +
                 "     var ws = new WebSocket(\"ws://" + host + "/websocket\");\n" +
                 "     ws.onopen = function()\n" +
                 "     {\n" +
                 "        // Web Socket is connected, send data using send()\n" +
                 "        ws.send(\"Message to send\");\n" +
                 "        alert(\"Message is sent...\");\n" +
                 "     };\n" +
                 "     ws.onmessage = function (evt) \n" +
                 "     { \n" +
                 "        var received_msg = evt.data;\n" +
                 "        alert(\"Message is received...\");\n" +
                 "     };\n" +
                 "     ws.onclose = function()\n" +
                 "     { \n" +
                 "        // websocket is closed.\n" +
                 "        alert(\"Connection is closed...\"); \n" +
                 "     };\n" +
                 "  }\n" +
                 "  else\n" +
                 "  {\n" +
                 "     // The browser doesn't support WebSocket\n" +
                 "     alert(\"WebSocket NOT supported by your Browser!\");\n" +
                 "  }\n" +
                 "}\n" +
                 "</script>\n" +
                 "</head>\n" +
                 "<body>\n" +
                 "<div id=\"sse\">\n" +
                 "   <a href=\"javascript:WebSocketTest()\">Run WebSocket</a>\n" +
                 "</div>\n" +
                 "</body>\n" +
                 "</html>";

         response.getOutputStream().write(page.getBytes("UTF-8"));

      }
   }

   public static void main(String[] args) throws Exception
   {
      HttpServer http = new HttpServerBuilder().connector().add()
              .workers(1)
              .maxRequestThreads(1).build();
      http.start();
      TextHandler handler = new TextHandler();
      http.register("/websocket", handler);
      http.register("/echo.html", new EchoHtml());

      try
      {
         Thread.sleep(1000000);
      }
      catch (InterruptedException e)
      {
      }
      http.stop();

   }
}
