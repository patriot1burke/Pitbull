package org.jbos.pitbull.test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.jboss.pitbull.stress.StressClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HttpServerStressClientTest
{
   public static HttpServer http;

   public static class StressServlet implements HttpHandler
   {
      @Override
      public void handle(HttpExchange exchange) throws IOException
      {
         if (exchange.getRequestMethod().equalsIgnoreCase("GET")) doGet(exchange);
         else if (exchange.getRequestMethod().equalsIgnoreCase("PUT")) doPut(exchange);
         else if (exchange.getRequestMethod().equalsIgnoreCase("POST")) doPost(exchange);
      }

      protected void doGet(HttpExchange exchange) throws IOException
      {
         exchange.getResponseHeaders().set("Content-Type", "text/plain");
         exchange.getResponseHeaders().set("Content-Length", Integer.toString("DO GET".getBytes().length));
         exchange.sendResponseHeaders(200, "DO GET".getBytes().length);
         exchange.getResponseBody().write("DO GET".getBytes());
         exchange.getResponseBody().close();
      }

      protected void doPut(HttpExchange exchange) throws IOException
      {
         exchange.sendResponseHeaders(204, -1);
         exchange.getResponseBody().close();
      }

      protected void doPost(HttpExchange exchange) throws IOException
      {
         byte[] bytes = TJWSStressClientTest.readFromStream(1024, exchange.getRequestBody());
         exchange.getResponseHeaders().set("Content-Type", "text/plain");
         exchange.getResponseHeaders().set("Content-Length", Integer.toString(bytes.length));
         exchange.sendResponseHeaders(200, bytes.length);
         exchange.getResponseBody().write(bytes);
         exchange.getResponseBody().close();
      }
   }


   @BeforeClass
   public static void startup() throws Exception
   {
      http = HttpServer.create(new InetSocketAddress(8080), 100);
      http.createContext("/", new StressServlet());
      http.start();

   }

   @AfterClass
   public static void shutdown() throws Exception
   {
      http.stop(0);
   }

   @Test
   public void testClientStress() throws Exception
   {
      StressClient.stress(1, 2);
   }

   //@Test
   public void testClientStressMultiple() throws Exception
   {
      System.out.println("************************");
      System.out.println(" HttpServer Socket Stress");
      System.out.println("************************");
      for (int i = 5; i < 21; i += 5)
      {
         System.out.println();
         System.out.println();
         System.out.println("-- Test with client thread num: " + (i + 1) * 3);
         StressClient.stress(i + 1, 5);
      }
   }

}
