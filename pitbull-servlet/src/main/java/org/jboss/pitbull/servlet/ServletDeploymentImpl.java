package org.jboss.pitbull.servlet;

import org.jboss.pitbull.util.registry.UriRegistry;
import org.jboss.pitbull.spi.Connection;
import org.jboss.pitbull.spi.RequestHandler;
import org.jboss.pitbull.spi.RequestHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ServletDeploymentImpl implements ServletDeployment
{
   protected String root;
   protected UriRegistry<Servlet> servletRegistry = new UriRegistry<Servlet>();
   protected List<Servlet> servlets = new ArrayList<Servlet>();

   public ServletDeploymentImpl(String root)
   {
      this.root = root;
   }

   @Override
   public String getRoot()
   {
      return root;
   }

   @Override
   public void addServlet(Servlet servlet)
   {
      servlets.add(servlet);
      servletRegistry.add(servlet.getUrlPattern(), servlet);
   }

   @Override
   public void removeServlet(Servlet servlet)
   {
      servlets.remove(servlet);
      servletRegistry.remove(servlet);
   }

   @Override
   public List<Servlet> getServlets()
   {
      return servlets;
   }

   @Override
   public RequestHandler begin(Connection connection, RequestHeader headers)
   {
      List<Servlet> servlets = null;
      try
      {
         servlets = servletRegistry.match(headers.getUri(), root.length());
      }
      catch (Exception e)
      {
         return null;
      }
      if (servlets.size() < 1) return null;
      Servlet servlet = servlets.get(0);
      return new ServletRequestHandler(connection, servlet);
   }
}
