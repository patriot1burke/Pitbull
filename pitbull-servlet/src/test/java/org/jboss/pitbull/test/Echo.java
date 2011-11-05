package org.jboss.pitbull.test;

import org.jboss.pitbull.servlet.DeploymentServletContext;
import org.jboss.pitbull.servlet.EmbeddedServletContainer;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class Echo extends HttpServlet
{
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      resp.setContentType("text/plain");
      resp.setStatus(200);
      try
      {
         Thread.sleep(50000);
      }
      catch (InterruptedException e)
      {
         throw new RuntimeException(e);
      }
      resp.getOutputStream().write("hello world".getBytes());
   }

   @Override
   protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      ServletInputStream is = req.getInputStream();
      String val = Util.readString(is, null);
      System.out.println(val);
      resp.setStatus(204);
   }

   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      ServletInputStream is = req.getInputStream();
      String val = Util.readString(is, null);
      System.out.println(val);
      resp.setStatus(200);
      resp.setContentType("text/plain");
      resp.getOutputStream().write(val.getBytes());

   }

   public static void main(String[] args) throws Exception
   {
      EmbeddedServletContainer server = new EmbeddedServletContainer();
      server.setNumWorkers(1);
      server.setNumExecutors(1);
      DeploymentServletContext ctx = server.newDeployment("");
      ctx.addServlet("fixed", new Echo()).addMapping("/*");
      server.start();
   }
}
