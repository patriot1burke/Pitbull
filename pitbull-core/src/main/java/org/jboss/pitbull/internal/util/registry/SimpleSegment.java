package org.jboss.pitbull.internal.util.registry;

import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SimpleSegment<T> extends ParentSegment<T>
{
   protected String segment;

   public SimpleSegment(String segment)
   {
      this.segment = segment;
   }

   public String getSegment()
   {
      return segment;
   }

   @Override
   protected void removeSegment()
   {
      parent.simpleSegments.remove(segment);
   }

   protected List<T> matchSimple(String path, int start)
   {
      if (start + segment.length() == path.length()) // we've reached end of string
      {
         if (matches == null || matches.size() < 1)
            throw new NotFoundException("Could not find resource for relative : " + path);

         return matches;
      }
      else
      {
         return matchChildren(path, start + segment.length() + 1); // + 1 to ignore '/'
      }
   }

   public List<T> matchMulti(String path, int start)
   {
      if (start + segment.length() == path.length()) // we've reached end of string
      {
         if (matches == null || matches.size() < 1)
            throw new NotFoundException("Could not find resource for relative : " + path);

         return matches;
      }
      else
      {
         return matchMultiChildren(path, start + segment.length() + 1); // + 1 to ignore '/'
      }
   }
}
