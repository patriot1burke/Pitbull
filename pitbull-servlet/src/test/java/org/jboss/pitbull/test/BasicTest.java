package org.jboss.pitbull.test;

import org.jboss.pitbull.netty.PitbullServer;
import org.jboss.pitbull.servlet.ServletDeployment;
import org.jboss.pitbull.servlet.ServletDeploymentImpl;
import org.jboss.pitbull.servlet.SimpleServlet;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class BasicTest
{
   protected static ServletDeployment deployment;
   protected static PitbullServer server;

   public static class FixedLengthServlet extends HttpServlet
   {
      @Override
      protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
      {
         resp.setContentType("text/plain");
         resp.setStatus(200);
         resp.getOutputStream().write("hello world".getBytes());
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
   }

   @BeforeClass
   public static void startup() throws Exception
   {
      deployment = new ServletDeploymentImpl("");
      server = new PitbullServer();
      server.getRegistry().add(deployment.getRoot() + "/{.*}", deployment);
      deployment.addServlet(new SimpleServlet("fixed", "/fixed{.*}", new FixedLengthServlet()));
      deployment.addServlet(new SimpleServlet("chunked", "/chunked{.*}", new ChunkedServlet()));
      server.start();
   }

   @AfterClass
   public static void shutdown() throws Exception
   {
      server.stop();
   }

   @Test
   public void testFixed() throws Exception
   {
      ClientRequest request = new ClientRequest("http://localhost:" + server.getPort() + "/fixed");
      ClientResponse response = request.get();
      Assert.assertEquals(200, response.getStatus());
      String cl = (String)response.getHeaders().getFirst("Content-Length");
      Assert.assertNotNull(cl);
      System.out.println("Content-Length: " + cl);
      Assert.assertEquals("hello world", response.getEntity(String.class));
   }

   @Test
   public void testChunked() throws Exception
   {
      ClientRequest request = new ClientRequest("http://localhost:" + server.getPort() + "/chunked");
      ClientResponse response = request.get();
      Assert.assertEquals(200, response.getStatus());
      String cl = (String)response.getHeaders().getFirst("Content-Length");
      System.out.println(cl);
      Assert.assertNull(cl);
      System.out.println("Entity: " + response.getEntity(String.class));
      System.out.println("Transfer-Encoding: " + response.getHeaders().getFirst("Transfer-Encoding"));
   }
}
