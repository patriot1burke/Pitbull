package org.jboss.pitbull.servlet;

import org.jboss.pitbull.nio.http.PitbullServer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class EmbeddedServletContainer extends PitbullServer
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
         registry.add(deployment.getDeploymentServletContext().getContextPath() + "/{.*}", deployment);
      }
      for (ServletDeployment deployment : deployments)
      {
         deployment.start();
      }
      super.start();
   }
}
