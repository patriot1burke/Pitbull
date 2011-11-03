package org.jboss.pitbull.servlet;

import javax.servlet.Registration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
abstract public class DeploymentRegistration implements Registration.Dynamic
{
   protected String name;
   protected DeploymentServletContext servletContext;
   protected Map<String, String> initParameters = new HashMap<String, String>();
   protected boolean asyncSupported;

   protected void checkNullParameter(Object param)
   {
      if (param == null) throw new IllegalArgumentException("Parameter was null");
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public boolean setInitParameter(String name, String value)
   {
      if (initParameters.containsKey(name)) return false;
      initParameters.put(name, value);
      return true;
   }

   @Override
   public String getInitParameter(String name)
   {
      return initParameters.get(name);
   }

   @Override
   public Set<String> setInitParameters(Map<String, String> initParameters)
   {
      Set<String> already = new HashSet<String>();
      for (Map.Entry<String, String> entry : initParameters.entrySet())
      {
         if (entry.getKey() == null || entry.getValue() == null)
         {
            throw new IllegalArgumentException("Null value in initParameters map not allowed");
         }
         if (this.initParameters.containsKey(entry.getKey())) already.add(entry.getKey());
      }
      if (already.size() > 0) return already;
      this.initParameters.putAll(initParameters);
      return already;
   }

   @Override
   public Map<String, String> getInitParameters()
   {
      return initParameters;
   }

   @Override
   public void setAsyncSupported(boolean isAsyncSupported)
   {
      asyncSupported = isAsyncSupported;
   }
}
