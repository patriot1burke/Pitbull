package org.jboss.pitbull.servlet;

import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class DefaultInstanceManager implements InstanceManager
{
   protected ClassLoader classLoader;

   public DefaultInstanceManager(ClassLoader classLoader)
   {
      this.classLoader = classLoader;
   }

   @Override
   public Object newInstance(String fqcn) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException
   {
      return classLoader.loadClass(fqcn).newInstance();
   }

   @Override
   public Object newInstance(Class<?> c) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException
   {
      return c.newInstance();
   }

   @Override
   public void inject(Object o) throws IllegalAccessException, InvocationTargetException, NamingException
   {
   }

   @Override
   public void destroyInstance(Object o) throws IllegalAccessException, InvocationTargetException
   {
   }
}
