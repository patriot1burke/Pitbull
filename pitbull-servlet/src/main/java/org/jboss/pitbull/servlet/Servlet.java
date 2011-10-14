package org.jboss.pitbull.servlet;

import javax.servlet.http.HttpServlet;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface Servlet
{
   String getName();
   String getUrlPattern();
   HttpServlet getServletInstance();
}
