package org.jboss.pitbull.servlet;


import javax.servlet.HttpMethodConstraintElement;
import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;
import javax.servlet.SingleThreadModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class DeploymentServletRegistration extends DeploymentRegistration implements ServletRegistration.Dynamic
{
   protected int loadLevel;
   protected ServletSecurityElement constraint;
   protected MultipartConfigElement multipartConfig;
   protected String runAsRole;
   protected Servlet servlet;
   protected String className;
   protected Class<? extends Servlet> servletClass;
   protected List<String> urlPatterns = new ArrayList<String>();
   protected List<Pattern> patterns = new ArrayList<Pattern>();
   protected InstanceManager im;
   protected ServletConfig config;
   protected boolean initialized;
   protected boolean perRequest;

   public DeploymentServletRegistration(String name, String className, DeploymentServletContext ctx)
   {
      this.name = name;
      this.className = className;
      this.servletContext = ctx;
   }

   public DeploymentServletRegistration(String name, Class<? extends Servlet> servletClass, DeploymentServletContext ctx)
   {
      this.name = name;
      this.servletClass = servletClass;
      this.servletContext = ctx;
   }

   public DeploymentServletRegistration(String name, Servlet servlet, DeploymentServletContext ctx)
   {
      this.name = name;
      this.servlet = servlet;
      this.servletContext = ctx;
   }

   public boolean matchesPattern(String pattern)
   {
      pattern = pattern.replace("*", "{WILDCARD}");
      for (Pattern p : patterns)
      {
         if (p.matcher(pattern).matches()) return true;
      }
      return false;

   }

   public void initialize(InstanceManager im, ClassLoader loader) throws Exception
   {
      this.im = im;
      this.config = new ServletConfig()
      {
         @Override
         public String getServletName()
         {
            return DeploymentServletRegistration.this.name;
         }

         @Override
         public ServletContext getServletContext()
         {
            return DeploymentServletRegistration.this.servletContext;
         }

         @Override
         public String getInitParameter(String name)
         {
            return DeploymentServletRegistration.this.getInitParameter(name);
         }

         @Override
         public Enumeration<String> getInitParameterNames()
         {
            final Iterator<String> it = DeploymentServletRegistration.this.initParameters.keySet().iterator();
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
      };
      if (servlet == null && servletClass == null)
      {
         servletClass = (Class<? extends Servlet>) loader.loadClass(className);
      }
      if (servlet != null)
      {
         servletClass = servlet.getClass();
      }
      if (SingleThreadModel.class.isAssignableFrom(servletClass))
      {
         perRequest = true;
      }
      if (loadLevel >= 0 && !perRequest)
      {
         initializeServlet();
      }
   }

   protected void initializeServlet() throws Exception
   {
      if (servlet != null)
      {
         this.im.inject(servlet);
      }
      else
      {
         servlet = (Servlet) this.im.newInstance(servletClass);
      }
      servlet.init(config);
      initialized = true;
   }

   public Servlet startRequest() throws Exception
   {
      if (initialized) return servlet;
      initializeServlet();
      return servlet;
   }

   public void endRequest() throws Exception
   {
      if (perRequest)
      {
         this.im.destroyInstance(servlet);
         servlet = null;
         initialized = false;
      }
   }

   public int getLoadLevel()
   {
      return loadLevel;
   }

   public ServletSecurityElement getConstraint()
   {
      return constraint;
   }

   public MultipartConfigElement getMultipartConfig()
   {
      return multipartConfig;
   }

   public boolean isAsyncSupported()
   {
      return asyncSupported;
   }

   public List<Pattern> getPatterns()
   {
      return patterns;
   }

   @Override
   public void setLoadOnStartup(int loadOnStartup)
   {
      loadLevel = loadOnStartup;
   }

   @Override
   public Set<String> setServletSecurity(ServletSecurityElement constraint)
   {
      checkNullParameter(constraint);
      Set<String> already = new HashSet<String>();
      for (String urlPattern : urlPatterns)
      {
         Set<String> methodNames = new HashSet<String>();
         methodNames.addAll(constraint.getMethodNames());
         for (HttpMethodConstraintElement methodConstraint : constraint.getHttpMethodConstraints())
         {
            methodNames.add(methodConstraint.getMethodName());
         }
         already.addAll(servletContext.matchSecurityConstraint(urlPattern, methodNames));
      }
      if (already.size() > 0) return already;
      this.constraint = constraint;
      return already;
   }

   @Override
   public void setMultipartConfig(MultipartConfigElement multipartConfig)
   {
      checkNullParameter(multipartConfig);
      this.multipartConfig = multipartConfig;
   }

   @Override
   public void setRunAsRole(String roleName)
   {
      checkNullParameter(roleName);
      this.runAsRole = roleName;
   }

   @Override
   public String getRunAsRole()
   {
      return runAsRole;
   }

   @Override
   public String getClassName()
   {
      if (className != null) return className;
      if (servlet != null) return servlet.getClass().getName();
      if (servletClass != null) return servletClass.getName();
      return null;
   }

   @Override
   public Set<String> addMapping(String... urlPatterns)
   {
      Set<String> matches = new HashSet<String>();
      for (String urlPattern : urlPatterns)
      {
         if (servletContext.matchesServletUrlPattern(urlPattern))
         {
            matches.add(urlPattern);
         }

      }
      if (matches.isEmpty())
      {
         for (String urlPattern : urlPatterns)
         {
            this.urlPatterns.add(urlPattern);
            urlPattern = urlPattern.replace("*", ".*");
            this.patterns.add(Pattern.compile(urlPattern));
         }
      }
      return matches;
   }

   @Override
   public Collection<String> getMappings()
   {
      return urlPatterns;
   }


}
