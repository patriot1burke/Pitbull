package org.jboss.pitbull.servlet;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class DeploymentSecurityConstraint
{
   public enum TransportGuarantee
   {
      NONE,
      INTEGRAL,
      CONFIDENTIAL
   }

   protected List<WebResourceCollection> webResources = new ArrayList<WebResourceCollection>();
   protected Set<String> authConstraints = new HashSet<String>();
   protected TransportGuarantee transportGuarantee = TransportGuarantee.NONE;

   public List<WebResourceCollection> getWebResources()
   {
      return webResources;
   }

   public DeploymentSecurityConstraint guarantee(TransportGuarantee guarantee)
   {
      transportGuarantee = guarantee;
      return this;
   }

   public DeploymentSecurityConstraint authConstraint(String... authConstraints)
   {
      for (String role : authConstraints)
      {
         this.authConstraints.add(role);
      }
      return this;
   }

   public WebResourceCollection resource(String... patterns)
   {
      WebResourceCollection collection = new WebResourceCollection(this).patterns(patterns);
      webResources.add(collection);
      return collection;
   }

   public Set<String> getAuthConstraints()
   {
      return authConstraints;
   }

   public TransportGuarantee getTransportGuarantee()
   {
      return transportGuarantee;
   }

   public Set<String> matches(String path, Collection<String> methods)
   {
      HashSet<String> set = new HashSet<String>();
      for (WebResourceCollection col : webResources)
      {
         set.addAll(col.matches(path, methods));
      }
      return set;
   }
}
