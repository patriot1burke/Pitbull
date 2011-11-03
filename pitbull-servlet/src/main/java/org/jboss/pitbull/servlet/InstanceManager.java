package org.jboss.pitbull.servlet;

import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface InstanceManager
{
   public Object newInstance(String fqcn)
           throws IllegalAccessException, InvocationTargetException, NamingException,
           InstantiationException, ClassNotFoundException;

   public Object newInstance(Class<?> c)
           throws IllegalAccessException, InvocationTargetException, NamingException,
           InstantiationException;

   public void inject(Object o)
           throws IllegalAccessException, InvocationTargetException, NamingException;

   public void destroyInstance(Object o)
           throws IllegalAccessException, InvocationTargetException;
}
