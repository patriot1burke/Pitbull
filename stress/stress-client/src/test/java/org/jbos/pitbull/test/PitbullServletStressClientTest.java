package org.jbos.pitbull.test;

import org.jboss.pitbull.servlet.DeploymentServletContext;
import org.jboss.pitbull.servlet.EmbeddedServletContainer;
import org.jboss.pitbull.servlet.EmbeddedServletContainerBuilder;
import org.jboss.pitbull.stress.StressClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Stress against PitbulServlet
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PitbullServletStressClientTest
{

   public static byte[] readFromStream(int bufferSize, InputStream entityStream)
           throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      byte[] buffer = new byte[bufferSize];
      int wasRead = 0;
      do
      {
         wasRead = entityStream.read(buffer);
         if (wasRead > 0)
         {
            baos.write(buffer, 0, wasRead);
         }
      } while (wasRead > -1);
      return baos.toByteArray();
   }

   public static class StressServlet extends HttpServlet
   {
      @Override
      protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
      {
         resp.setStatus(200);
         resp.setContentType("text/plain");
         resp.getOutputStream().write("DO GET".getBytes());
      }

      @Override
      protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
      {
         resp.setStatus(204);
      }

      @Override
      protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
      {
         byte[] bytes = readFromStream(1024, req.getInputStream());
         resp.setStatus(200);
         resp.setContentType("text/plain");
         resp.getOutputStream().write(bytes);
      }
   }

   protected static EmbeddedServletContainer server;
   protected static int PORT = 8080;

   @BeforeClass
   public static void startup() throws Exception
   {
      server = new EmbeddedServletContainerBuilder()
              .connector().port(PORT).add()
              .maxRequestThreads(4)
              .workers(2)
              .build();
      DeploymentServletContext ctx = server.newDeployment("");
      ctx.addServlet("stress", new StressServlet()).addMapping("/*");
      server.start();

   }

   @AfterClass
   public static void shutdown() throws Exception
   {
      server.stop();
   }

   @Test
   public void testClientStressMultiple() throws Exception
   {
      System.out.println("************************");
      System.out.println(" Pitbull Servlet Stress");
      System.out.println("************************");
      for (int i = 5; i < 21; i+= 5)
      {
         System.out.println();
         System.out.println();
         System.out.println("-- Test with client thread num: " + (i + 1) * 3);
         StressClient.stress(i + 1, 5);
      }
   }

}
