package org.jboss.pitbull.servlet;

import javax.servlet.http.HttpServlet;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface Filter
{
   String getName();
   String getUrlPattern();
   javax.servlet.Filter getFilterInstance();
}
