package org.jboss.pitbull.servlet;

import org.jboss.pitbull.Connection;
import org.jboss.pitbull.RequestHeader;
import org.jboss.pitbull.servlet.internal.ServletRequestHandler;
import org.jboss.pitbull.server.spi.RequestHandler;
import org.jboss.pitbull.server.spi.RequestInitiator;
import org.jboss.pitbull.util.registry.UriRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ServletDeployment implements RequestInitiator
{
   protected UriRegistry<DeploymentServletRegistration> servletRegistry = new UriRegistry<DeploymentServletRegistration>();
   protected InstanceManager im;
   protected DeploymentServletContext ctx;
   protected ClassLoader classLoader;


   public ServletDeployment(String root)
   {
      this.ctx = new DeploymentServletContext();
      this.ctx.setContextPath(root);
   }

   public DeploymentServletContext getDeploymentServletContext()
   {
      return ctx;
   }

   public void start() throws Exception
   {
      if (classLoader == null) classLoader = Thread.currentThread().getContextClassLoader();
      if (im == null) im = new DefaultInstanceManager(classLoader);
      Comparator<DeploymentServletRegistration> comparator = new Comparator<DeploymentServletRegistration>()
      {
         @Override
         public int compare(DeploymentServletRegistration a, DeploymentServletRegistration b)
         {
            if (a.getLoadLevel() == b.getLoadLevel()) return 0;
            if (a.getLoadLevel() > b.getLoadLevel()) return 1;
            return -1;
         }


      };

      List<DeploymentServletRegistration> servlets = new ArrayList<DeploymentServletRegistration>();
      Collection<DeploymentServletRegistration> values = (Collection<DeploymentServletRegistration>) ctx.getServletRegistrations().values();
      servlets.addAll(values);
      Collections.sort(servlets, comparator);

      for (DeploymentServletRegistration reg : servlets)
      {
         reg.initialize(im, classLoader);
         for (String mapping : reg.getMappings())
         {
            mapping = mapping.replace("*", "{.*}");
            servletRegistry.register(mapping, reg);
         }
      }
      ctx.setInitialized();
   }

   @Override
   public void illegalHandler(RequestHandler handler)
   {
   }

   @Override
   public RequestHandler begin(Connection connection, RequestHeader headers)
   {
      List<DeploymentServletRegistration> servlets = null;
      try
      {
         servlets = servletRegistry.match(headers.getUri(), ctx.getContextPath().length());
      }
      catch (Exception e)
      {
         return null;
      }
      if (servlets.size() < 1) return null;
      DeploymentServletRegistration servlet = servlets.get(0);
      return new ServletRequestHandler(servlet, ctx);
   }
}
