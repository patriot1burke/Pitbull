package org.jboss.pitbull.servlet;

import org.jboss.pitbull.util.registry.UriRegistry;
import org.jboss.pitbull.spi.RequestInitiator;

import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ServletContainer
{
   void addDeployment(ServletDeployment deployment);
   void removeDeployment(ServletDeployment deployment);
   List<ServletDeployment> getDeployments();
}
