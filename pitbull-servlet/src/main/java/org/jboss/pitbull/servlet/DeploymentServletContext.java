package org.jboss.pitbull.servlet;

import org.jboss.pitbull.NotImplementedYetException;
import org.jboss.pitbull.internal.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class DeploymentServletContext implements ServletContext
{
   protected Map<String, Object> attributes = new HashMap<String, Object>();
   protected Map<String, String> initParams = new HashMap<String, String>();
   protected String contextPath;
   protected static final Logger log = Logger.getLogger(ServletContext.class);
   protected List<DeploymentSecurityConstraint> securityConstraints = new ArrayList<DeploymentSecurityConstraint>();
   protected Map<String, DeploymentServletRegistration> servletRegistrations = new HashMap<String, DeploymentServletRegistration>();
   protected boolean initialized;

   public void setInitialized()
   {
      initialized = true;
   }

   public boolean isInitialized()
   {
      return initialized;
   }

   public DeploymentSecurityConstraint securityConstraint()
   {
      DeploymentSecurityConstraint constraint = new DeploymentSecurityConstraint();
      securityConstraints.add(constraint);
      return constraint;
   }

   public Set<String> matchSecurityConstraint(String path, Collection<String> methods)
   {
      Set<String> already = new HashSet<String>();
      for (DeploymentSecurityConstraint constraint : securityConstraints)
      {
         already.addAll(constraint.matches(path, methods));
      }
      return already;
   }

   public boolean matchesServletUrlPattern(String urlPattern)
   {
      for (DeploymentServletRegistration reg : servletRegistrations.values())
      {
         if (reg.matchesPattern(urlPattern)) return true;
      }
      return false;
   }

   public void setContextPath(String path)
   {
      this.contextPath = path;
   }

   @Override
   public String getContextPath()
   {
      return contextPath;
   }

   @Override
   public ServletContext getContext(String uripath)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public int getMajorVersion()
   {
      return 3;
   }

   @Override
   public int getMinorVersion()
   {
      return 0;
   }

   @Override
   public int getEffectiveMajorVersion()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public int getEffectiveMinorVersion()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public String getMimeType(String file)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public Set<String> getResourcePaths(String path)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public URL getResource(String path) throws MalformedURLException
   {
      throw new NotImplementedYetException();
   }

   @Override
   public InputStream getResourceAsStream(String path)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public RequestDispatcher getRequestDispatcher(String path)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public RequestDispatcher getNamedDispatcher(String name)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public Servlet getServlet(String name) throws ServletException
   {
      throw new NotImplementedYetException();
   }

   @Override
   public Enumeration<Servlet> getServlets()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public Enumeration<String> getServletNames()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public void log(String msg)
   {
      log.info(msg);
   }

   @Override
   public void log(Exception exception, String msg)
   {
      log.error(msg, exception);
   }

   @Override
   public void log(String message, Throwable throwable)
   {
      log.error(message, throwable);
   }

   @Override
   public String getRealPath(String path)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public String getServerInfo()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public String getInitParameter(String name)
   {
      return initParams.get(name);
   }

   @Override
   public Enumeration<String> getInitParameterNames()
   {
      final Iterator<String> it = initParams.keySet().iterator();
      return new Enumeration<String>()
      {
         @Override
         public boolean hasMoreElements()
         {
            return it.hasNext();
         }

         @Override
         public String nextElement()
         {
            return it.next();
         }
      };
   }

   @Override
   public boolean setInitParameter(String name, String value)
   {
      initParams.put(name, value);
      return true;
   }

   @Override
   public Object getAttribute(String name)
   {
      return attributes.get(name);
   }

   @Override
   public Enumeration<String> getAttributeNames()
   {
      final Iterator<String> it = attributes.keySet().iterator();
      return new Enumeration<String>()
      {
         @Override
         public boolean hasMoreElements()
         {
            return it.hasNext();
         }

         @Override
         public String nextElement()
         {
            return it.next();
         }
      };
   }

   @Override
   public void setAttribute(String name, Object object)
   {
      attributes.put(name, object);
   }

   @Override
   public void removeAttribute(String name)
   {
      attributes.remove(name);
   }

   @Override
   public String getServletContextName()
   {
      throw new NotImplementedYetException();
   }

   public ServletRegistration.Dynamic addServlet(
           String servletName, String className)
   {
      DeploymentServletRegistration reg = new DeploymentServletRegistration(servletName, className, this);
      servletRegistrations.put(servletName, reg);
      return reg;
   }

   public ServletRegistration.Dynamic addServlet(
           String servletName, Servlet servlet)
   {
      DeploymentServletRegistration reg = new DeploymentServletRegistration(servletName, servlet, this);
      servletRegistrations.put(servletName, reg);
      return reg;
   }


   @Override
   public ServletRegistration.Dynamic addServlet(String servletName,
                                                 Class<? extends Servlet> servletClass)
   {
      DeploymentServletRegistration reg = new DeploymentServletRegistration(servletName, servletClass, this);
      servletRegistrations.put(servletName, reg);
      return reg;
   }

   @Override
   public ServletRegistration getServletRegistration(String servletName)
   {
      return servletRegistrations.get(servletName);
   }

   @Override
   public Map<String, ? extends ServletRegistration> getServletRegistrations()
   {
      return servletRegistrations;
   }

   @Override
   public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException
   {
      throw new NotImplementedYetException();
   }

   @Override
   public FilterRegistration.Dynamic addFilter(String filterName, String className)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public FilterRegistration.Dynamic addFilter(String filterName, Filter filter)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException
   {
      throw new NotImplementedYetException();
   }

   @Override
   public FilterRegistration getFilterRegistration(String filterName)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public Map<String, ? extends FilterRegistration> getFilterRegistrations()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public SessionCookieConfig getSessionCookieConfig()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public Set<SessionTrackingMode> getDefaultSessionTrackingModes()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public Set<SessionTrackingMode> getEffectiveSessionTrackingModes()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public void addListener(String className)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public <T extends EventListener> void addListener(T t)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public void addListener(Class<? extends EventListener> listenerClass)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException
   {
      throw new NotImplementedYetException();
   }

   @Override
   public JspConfigDescriptor getJspConfigDescriptor()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public ClassLoader getClassLoader()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public void declareRoles(String... roleNames)
   {
      throw new NotImplementedYetException();
   }
}
