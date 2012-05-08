package org.jboss.pitbull.test;

import org.jboss.pitbull.Connection;
import org.jboss.pitbull.HttpServer;
import org.jboss.pitbull.HttpServerBuilder;
import org.jboss.pitbull.RequestHeader;
import org.jboss.pitbull.StatusCode;
import org.jboss.pitbull.client.ClientConnection;
import org.jboss.pitbull.client.ClientConnectionFactory;
import org.jboss.pitbull.client.ClientInvocation;
import org.jboss.pitbull.handlers.stream.StreamRequestHandler;
import org.jboss.pitbull.handlers.stream.StreamedResponse;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.ReadFromStream;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class EchoTest
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

   public static class Initiator implements StreamRequestHandler
   {
      @Override
      public void execute(Connection connection, RequestHeader requestHeader, InputStream is, StreamedResponse response) throws IOException
      {
         System.out.println(requestHeader.getMethod() + " " + requestHeader.getUri());
         response.setStatus(StatusCode.OK);
         response.getHeaders().addHeader("Content-Type", "text/plain");

         if (requestHeader.getMethod().equalsIgnoreCase("POST"))
         {
            byte[] bytes = ReadFromStream.readFromStream(1024, is);
            response.getOutputStream().write(bytes);
         }
         else if (requestHeader.getMethod().equalsIgnoreCase("GET"))
         {
            response.getOutputStream().write("How are you".getBytes());
         }
      }
   }


   @Test
   public void test404() throws Exception
   {
      ClientRequest request = new ClientRequest("http://localhost:8080/notfound");
      ClientResponse res = request.get();
      Assert.assertEquals(404, res.getStatus());
   }

   @Test
   public void testEcho() throws Exception
   {
      Initiator resource = new Initiator();
      http.register("/echo", resource);

      //Thread.sleep(100000000);

      //http.getRegistry().add("/echo/{.*}", resource);

      try
      {
         ClientRequest request = new ClientRequest("http://localhost:8080/echo");
         ClientResponse res = request.body("text/plain", "hello world").post();
         Assert.assertEquals(200, res.getStatus());
         Assert.assertEquals("hello world", res.getEntity(String.class));
      }
      finally
      {
         http.unregister(resource);

      }

   }

   @Test
   public void testPitbullClient() throws Exception
   {
      Initiator resource = new Initiator();
      http.register("/echo", resource);

      try
      {
         ClientConnection connection = ClientConnectionFactory.http("localhost", 8080);
         try
         {
            ClientInvocation invocation = connection.request("/echo").get();
            org.jboss.pitbull.client.ClientResponse response = invocation.response();

            Assert.assertEquals(StatusCode.OK, response.getStatus());
            InputStream is = response.getResponseBody();
            byte[] bytes = ReadFromStream.readFromStream(1024, is);
            String val = new String(bytes);
            Assert.assertEquals("How are you", val);

            invocation = connection.request("/echo").post();
            invocation.getRequestBody().write("hello world".getBytes());
            response = invocation.response();
            Assert.assertEquals(StatusCode.OK, response.getStatus());
            is = response.getResponseBody();
            bytes = ReadFromStream.readFromStream(1024, is);
            val = new String(bytes);
            Assert.assertEquals("hello world", val);
         }
         finally
         {
            connection.close();
         }
      }
      finally
      {
         http.unregister(resource);

      }


   }

}
