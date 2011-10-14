package org.jboss.pitbull.servlet;

import org.jboss.pitbull.util.registry.UriRegistry;
import org.jboss.pitbull.spi.Connection;
import org.jboss.pitbull.spi.RequestHeader;
import org.jboss.pitbull.spi.RequestHandler;
import org.jboss.pitbull.spi.RequestInitiator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ServletContainerImpl implements ServletContainer
{
   protected List<ServletDeployment> deployments = new ArrayList<ServletDeployment>();

   @Override
   public void addDeployment(ServletDeployment deployment)
   {
      deployments.add(deployment);
   }

   @Override
   public void removeDeployment(ServletDeployment deployment)
   {
      deployments.remove(deployment);
   }

   @Override
   public List<ServletDeployment> getDeployments()
   {
      return deployments;
   }

}
