package org.jboss.pitbull.servlet.internal.session;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class DefaultHttpSession implements HttpSession, Serializable
{
   private long creationTime = System.currentTimeMillis();
   private String id;
   private long lastAccessTime = System.currentTimeMillis();
   private boolean invalidated;
   private Map<String, Object> attributes = new HashMap<String, Object>();
   private transient ServletContext servletContext;
   private int maxIntervalTime = 300;

   public DefaultHttpSession()
   {
   }



   void checkInvalidation()
   {
      if (invalidated) throw new IllegalStateException("HttpSession was invalidated.");
   }

   void updateAccessTime()
   {
      lastAccessTime = System.currentTimeMillis();
   }

   @Override
   public long getCreationTime()
   {
      return creationTime;
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public long getLastAccessedTime()
   {
      return lastAccessTime;
   }

   @Override
   public ServletContext getServletContext()
   {
      return null;
   }

   public void setServletContext(ServletContext servletContext)
   {
      this.servletContext = servletContext;
   }

   @Override
   public void setMaxInactiveInterval(int interval)
   {
      this.maxIntervalTime = interval;
   }

   @Override
   public int getMaxInactiveInterval()
   {
      return maxIntervalTime;
   }

   @Override
   public HttpSessionContext getSessionContext()
   {
      return new HttpSessionContext()
      {
         @Override
         public HttpSession getSession(String sessionId)
         {
            return DefaultHttpSession.this;
         }

         @Override
         public Enumeration<String> getIds()
         {
            return new Enumeration<String>()
            {
               @Override
               public boolean hasMoreElements()
               {
                  return false;
               }

               @Override
               public String nextElement()
               {
                  return null;
               }
            };
         }
      };
   }

   @Override
   public Object getAttribute(String name)
   {
      return attributes.get(name);
   }

   @Override
   public Object getValue(String name)
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
   public String[] getValueNames()
   {
      String[] names = new String[attributes.keySet().size()];

      return new String[0];
   }

   @Override
   public void setAttribute(String name, Object value)
   {
   }

   @Override
   public void putValue(String name, Object value)
   {
   }

   @Override
   public void removeAttribute(String name)
   {
   }

   @Override
   public void removeValue(String name)
   {
   }

   @Override
   public void invalidate()
   {
   }

   @Override
   public boolean isNew()
   {
      return false;
   }
}
