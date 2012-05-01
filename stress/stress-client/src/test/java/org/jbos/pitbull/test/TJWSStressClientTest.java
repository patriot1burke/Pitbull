package org.jbos.pitbull.test;

import Acme.Serve.Serve;
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
import java.util.Properties;

/**
 * Stress against TJWS to compare
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class TJWSStressClientTest
{
   public static Serve http;

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

   @BeforeClass
   public static void startup() throws Exception
   {
      http = new Serve();
      Properties props = new Properties();
      props.put(Serve.ARG_PORT, "8080");
      props.put(Serve.ARG_THREAD_POOL_SIZE, Integer.toString(100));
      props.put(Serve.ARG_MAX_CONN_USE, Integer.toString(100));
      props.put(Serve.ARG_KEEPALIVE, Boolean.toString(true));
      props.put(Serve.ARG_KEEPALIVE_TIMEOUT, Long.toString(100000000));


      http.addServlet("", new StressServlet());

      http.runInBackground();

   }

   @AfterClass
   public static void shutdown() throws Exception
   {
      http.stopBackground();
   }

   @Test
   public void testClientStress() throws Exception
   {
      StressClient.stress(1, 2);
   }

   //@Test
   public void testClientStressMultiple() throws Exception
   {
      for (int i = 0; i < 20; i++)
      {
         System.out.println();
         System.out.println();
         System.out.println("-- Test with client thread num: " + (i + 1) * 3);
         StressClient.stress(i + 1, 5);
      }
   }

}
