package org.jboss.pitbull.servlet;

import org.jboss.pitbull.spi.RequestInitiator;

import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ServletDeployment extends RequestInitiator
{
   String getRoot();

   void addServlet(Servlet servlet);

   void removeServlet(Servlet servlet);

   List<Servlet> getServlets();
}
