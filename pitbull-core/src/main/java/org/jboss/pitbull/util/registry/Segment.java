package org.jboss.pitbull.util.registry;


import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class Segment<T>
{
   protected List<T> matches = new ArrayList<T>();
   protected ParentSegment parent;

   protected abstract void removeSegment();

   protected void setParent(ParentSegment parent)
   {
      this.parent = parent;
   }

   public ParentSegment getParent()
   {
      return parent;
   }

   public List<T> getMatches()
   {
      return matches;
   }

   protected void addMatch(T item)
   {
      matches.add(item);
   }

   protected void removeMatch(T item)
   {
      matches.remove(item);
      if (matches.size() == 0)
      {
         removeSegment();
      }
   }

}
