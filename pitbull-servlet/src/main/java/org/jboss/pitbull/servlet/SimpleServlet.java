package org.jboss.pitbull.servlet;

import javax.servlet.http.HttpServlet;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SimpleServlet implements ServletReference
{
   protected String name;
   protected String urlPattern;
   protected HttpServlet servlet;

   public SimpleServlet(String name, String urlPattern, HttpServlet servlet)
   {
      this.name = name;
      this.urlPattern = urlPattern;
      this.servlet = servlet;
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public String getUrlPattern()
   {
      return urlPattern;
   }

   @Override
   public HttpServlet getServletInstance()
   {
      return servlet;
   }
}
