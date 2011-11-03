package org.jboss.pitbull.servlet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class WebResourceCollection
{
   protected String name;
   protected String description;
   protected Set<String> methods = new HashSet<String>();
   protected List<String> urlPatterns = new ArrayList<String>();
   protected List<Pattern> patterns = new ArrayList<Pattern>();
   protected DeploymentSecurityConstraint parent;

   public WebResourceCollection(DeploymentSecurityConstraint parent)
   {
      this.parent = parent;
   }

   public DeploymentSecurityConstraint getParent()
   {
      return parent;
   }

   public String getName()
   {
      return name;
   }

   public String getDescription()
   {
      return description;
   }

   public WebResourceCollection patterns(String... patterns)
   {
      for (String pattern : patterns)
      {
         pattern = pattern.replace("*", ".*");
         this.patterns.add(Pattern.compile(pattern));
         urlPatterns.add(pattern);
      }
      return this;
   }

   public WebResourceCollection methods(String... methods)
   {
      if (methods == null) this.methods.clear();
      if (methods.length == 0) this.methods.clear();
      for (String m : methods)
      {
         this.methods.add(m);
      }
      return this;
   }

   public WebResourceCollection name(String name)
   {
      this.name = name;
      return this;
   }

   public WebResourceCollection description(String desc)
   {
      this.description = desc;
      return this;
   }

   public Set<String> matches(String path, Collection<String> methods)
   {
      Set<String> matches = new HashSet<String>();
      for (int i = 0; i < urlPatterns.size(); i++)
      {
         if (patterns.get(i).matcher(path).matches())
         {
            if (methods.size() == 0) matches.add(urlPatterns.get(i));
            else
            {
               for (String method : methods)
               {
                  if (this.methods.contains(method)) matches.add(urlPatterns.get(i));
               }
            }
         }
      }
      return matches;
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

   public boolean matchesMethod(String method)
   {
      if (methods.size() == 0) return true;
      return methods.contains(method);
   }
}
