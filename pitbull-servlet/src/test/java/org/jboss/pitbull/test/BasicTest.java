package org.jboss.pitbull.test;

import org.jboss.pitbull.servlet.DeploymentServletContext;
import org.jboss.pitbull.servlet.EmbeddedServletContainer;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Test basic HTTP processing
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class BasicTest
{
   protected static EmbeddedServletContainer server;


   public static class FixedLengthServlet extends HttpServlet
   {
      @Override
      protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
      {
         resp.setContentType("text/plain");
         resp.setStatus(200);
         resp.getOutputStream().write("hello world".getBytes());
      }

      @Override
      protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
      {
         ServletInputStream is = req.getInputStream();
         String val = Util.readString(is, null);
         Assert.assertEquals("hello world", val);
         resp.setStatus(204);

      }
   }

   public static class ChunkedServlet extends HttpServlet
   {
      @Override
      protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
      {
         resp.setContentType("text/plain");
         resp.setStatus(200);
         ServletOutputStream os = resp.getOutputStream();
         os.write("1st Chunk ".getBytes());
         os.flush();
         os.write("2nd chunk".getBytes());

      }

      @Override
      protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
      {
         Assert.assertNotNull(req.getHeader("Transfer-Encoding"));
         ServletInputStream is = req.getInputStream();
         String val = Util.readString(is, null);
         Assert.assertEquals("hello world, bonjeur, guten morgen, yo, goodbye, cheers", val);
         resp.setStatus(204);

      }
   }

   @BeforeClass
   public static void startup() throws Exception
   {
      server = new EmbeddedServletContainer();
      server.setNumWorkers(1);
      server.setNumExecutors(1);
      DeploymentServletContext ctx = server.newDeployment("");
      ctx.addServlet("fixed", new FixedLengthServlet()).addMapping("/fixed");
      ctx.addServlet("chunked", new ChunkedServlet()).addMapping("/chunked");
      server.start();
   }

   @AfterClass
   public static void shutdown() throws Exception
   {
      server.stop();
   }

   @Test
   public void testThreadedOutput() throws Exception
   {

      Runnable r = new Runnable()
      {
         @Override
         public void run()
         {
            try
            {
               ClientRequest request = new ClientRequest("http://localhost:" + server.getPort() + "/fixed");
               ClientResponse response = request.get();
               Assert.assertEquals(200, response.getStatus());
               String cl = (String) response.getHeaders().getFirst("Content-Length");
               Assert.assertNotNull(cl);
               System.out.println("Content-Length: " + cl);
               Assert.assertEquals("hello world", response.getEntity(String.class));
            }
            catch (Exception e)
            {

            }
         }
      };

      Thread[] threads = new Thread[10];

      long start = System.currentTimeMillis();

      for (int i = 0; i < 10; i++)
      {
         threads[i] = new Thread(r);
      }
      for (int i = 0; i < 10; i++)
      {
         threads[i].start();
      }
      for (int i = 0; i < 10; i++)
      {
         threads[i].join();
      }
      System.out.println("Time took: " + (System.currentTimeMillis() - start));
   }

   @Test
   public void testFixedOutput() throws Exception
   {
      ClientExecutor ex = new ApacheHttpClientExecutor();
      ClientRequest request = new ClientRequest("http://localhost:" + server.getPort() + "/fixed", ex);
      long start = System.currentTimeMillis();
      ClientResponse response = request.get();
      System.out.println("Got back: " + (System.currentTimeMillis() - start));
      Assert.assertEquals(200, response.getStatus());
      String cl = (String) response.getHeaders().getFirst("Content-Length");
      Assert.assertNotNull(cl);
      System.out.println("Content-Length: " + cl);
      Assert.assertEquals("hello world", response.getEntity(String.class));
      System.out.println("Done!");

      start = System.currentTimeMillis();
      for (int i = 0; i < 5; i++)
      {
         request = new ClientRequest("http://localhost:" + server.getPort() + "/fixed", ex);
         response = request.get();
         Assert.assertEquals("hello world", response.getEntity(String.class));
         response.releaseConnection();

      }
      System.out.println("loop: " + (System.currentTimeMillis() - start));


   }

   @Test
   public void testChunkedOutput() throws Exception
   {
      ClientRequest request = new ClientRequest("http://localhost:" + server.getPort() + "/chunked");
      ClientResponse response = request.get();
      Assert.assertEquals(200, response.getStatus());
      String cl = (String) response.getHeaders().getFirst("Content-Length");
      System.out.println(cl);
      Assert.assertNull(cl);
      System.out.println("Entity: " + response.getEntity(String.class));
      System.out.println("Transfer-Encoding: " + response.getHeaders().getFirst("Transfer-Encoding"));
   }

   @Test
   public void testFixedInput() throws Exception
   {
      ClientRequest request = new ClientRequest("http://localhost:" + server.getPort() + "/fixed");
      ClientResponse response = request.body("text/plain", "hello world").put();
      Assert.assertEquals(204, response.getStatus());
   }

   @Test
   public void testChunkedInput() throws Exception
   {
      URL postUrl = new URL("http://localhost:" + server.getPort() + "/chunked");
      HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
      connection.setChunkedStreamingMode(10);
      connection.setDoOutput(true);
      connection.setInstanceFollowRedirects(false);
      connection.setRequestMethod("PUT");
      connection.setRequestProperty("Content-Type", "application/xml");
      OutputStream os = connection.getOutputStream();
      os.write("hello world,".getBytes());
      os.flush();
      os.write(" bonjeur, guten morgen, yo, ".getBytes());
      os.flush();
      os.write("goodbye, cheers".getBytes());
      os.flush();
      Assert.assertEquals(204, connection.getResponseCode());
      connection.disconnect();
   }

   @Test
   public void testFixedInputError() throws Exception
   {
      ClientRequest request = new ClientRequest("http://localhost:" + server.getPort() + "/fixed");
      ClientResponse response = request.body("text/plain", "error").put();
      Assert.assertEquals(500, response.getStatus());
   }
}
