package org.jboss.pitbull.servlet;

import org.jboss.pitbull.HttpServer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class EmbeddedServletContainer extends HttpServer
{
   protected List<ServletDeployment> deployments = new ArrayList<ServletDeployment>();

   public DeploymentServletContext newDeployment(String root)
   {
      ServletDeployment deployment = new ServletDeployment(root);
      deployments.add(deployment);
      return deployment.getDeploymentServletContext();
   }

   public void start() throws Exception
   {
      for (ServletDeployment deployment : deployments)
      {
         String path = deployment.getDeploymentServletContext().getContextPath();
         if (path == null) path = "";
         if ("/".equals(path.trim())) path = "";
         registry.add(path + "/{.*}", deployment);
      }
      for (ServletDeployment deployment : deployments)
      {
         deployment.start();
      }
      super.start();
   }
}
