package org.jboss.pitbull.test;

import org.jboss.pitbull.server.HttpServer;
import org.jboss.pitbull.server.HttpServerBuilder;
import org.jboss.pitbull.stress.StressService;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class StressServiceTest
{
   public static HttpServer http;

   @BeforeClass
   public static void startup() throws Exception
   {
      http = new HttpServerBuilder().connector().add()
              .workers(1)
              .maxRequestThreads(1).build();
      http.start();
      http.register("/{.*}", new StressService());
   }

   @AfterClass
   public static void shutdown() throws Exception
   {
      http.stop();
   }

   @Test
   public void testPost() throws Exception
   {
      ClientRequest request = new ClientRequest("http://localhost:8080/echo");
      ClientResponse res = request.body("text/plain", "hello world").post();
      Assert.assertEquals(200, res.getStatus());
      Assert.assertEquals("hello world", res.getEntity(String.class));
   }

   @Test
   public void testPut() throws Exception
   {
      ClientRequest request = new ClientRequest("http://localhost:8080/echo");
      ClientResponse res = request.body("text/plain", "hello world").put();
      Assert.assertEquals(204, res.getStatus());
   }

   @Test
   public void testGet() throws Exception
   {
      ClientRequest request = new ClientRequest("http://localhost:8080/echo");
      ClientResponse res = request.get();
      Assert.assertEquals(200, res.getStatus());
      Assert.assertEquals("DO GET", res.getEntity(String.class));
   }
}
