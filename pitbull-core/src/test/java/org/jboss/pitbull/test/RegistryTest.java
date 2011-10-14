package org.jboss.pitbull.test;

import org.jboss.pitbull.util.registry.NotFoundException;
import org.jboss.pitbull.util.registry.UriRegistry;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RegistryTest
{

   @Test
   public void testSimpleRegistry() throws Exception
   {
      UriRegistry<String> registry = new UriRegistry<String>();

      registry.add("/foo", "/foo");
      registry.add("/foo/bar1", "/foo/bar1");
      registry.add("/foo/bar2", "/foo/bar2");
      registry.add("/foo/bar3", "/foo/bar3");

      List<String> match;
      match = registry.match("/foo");
      Assert.assertEquals(1, match.size());
      Assert.assertEquals("/foo", match.get(0));

      match = registry.match("/foo/bar1");
      Assert.assertEquals(1, match.size());
      Assert.assertEquals("/foo/bar1", match.get(0));

      match = registry.match("/foo/bar2");
      Assert.assertEquals(1, match.size());
      Assert.assertEquals("/foo/bar2", match.get(0));

      match = registry.match("/foo/bar3");
      Assert.assertEquals(1, match.size());
      Assert.assertEquals("/foo/bar3", match.get(0));

      try
      {
         match = registry.match("/foo/bar4");
         Assert.fail("Should have not found it");
         Assert.assertEquals(1, match.size());
         Assert.assertEquals("/foo/bar4", match.get(0));
      }
      catch (NotFoundException e)
      {
      }
   }

   @Test
   public void testSimpleAndPatternRegistry() throws Exception
   {
      UriRegistry<String> registry = new UriRegistry<String>();

      String star = "/*";
      registry.add("/{.*}", star);
      registry.add("/foo", "/foo");
      registry.add("/foo/bar", "/foo/bar");

      List<String> match;
      match = registry.match("/foo");
      Assert.assertEquals(1, match.size());
      Assert.assertEquals("/foo", match.get(0));

      match = registry.match("/foo/bar");
      Assert.assertEquals(1, match.size());
      Assert.assertEquals("/foo/bar", match.get(0));

      match = registry.match("/foo/bar2");
      Assert.assertEquals(1, match.size());
      Assert.assertEquals("/*", match.get(0));

      match = registry.match("/x/y/z/p/d/q/bar2");
      Assert.assertEquals(1, match.size());
      Assert.assertEquals("/*", match.get(0));

      registry.remove(star);
      try
      {
         match = registry.match("/x/y/z/p/d/q/bar2");
         Assert.fail("should be unreachable");
      }
      catch (NotFoundException e)
      {

      }

   }

   @Test
   public void testMultiSimpleAndPatternRegistry() throws Exception
   {
      UriRegistry<String> registry = new UriRegistry<String>();

      String fstar = "/f*";
      registry.add("/f{.*}", fstar);
      registry.add("/foo", "/foo");
      registry.add("/foo/bar", "/foo/bar");
      registry.add("/z{.*}", "/z*");

      List<String> match;
      match = registry.matchMulti("/foo");
      Assert.assertEquals(2, match.size());
      Assert.assertTrue(match.contains("/foo"));
      Assert.assertTrue(match.contains("/f*"));

      match = registry.matchMulti("/foo/bar");
      Assert.assertEquals(2, match.size());
      Assert.assertTrue(match.contains("/foo/bar"));
      Assert.assertTrue(match.contains("/f*"));

      match = registry.matchMulti("/foo/bar2");
      Assert.assertEquals(1, match.size());
      Assert.assertTrue(match.contains("/f*"));

      match = registry.matchMulti("/zfoo/bar");
      Assert.assertEquals(1, match.size());
      Assert.assertTrue(match.contains("/z*"));

      registry.remove(fstar);
      match = registry.matchMulti("/foo/bar2");
      Assert.assertEquals(0, match.size());

   }
}
