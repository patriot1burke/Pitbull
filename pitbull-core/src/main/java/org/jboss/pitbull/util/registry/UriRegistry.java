package org.jboss.pitbull.util.registry;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UriRegistry<T> extends ParentSegment<T>
{
   protected
   Map<T, Segment> globalRegistry = new HashMap<T, Segment>();

   @Override
   public void removeSegment()
   {
   }

   public Segment remove(T resource)
   {
      Segment segment = globalRegistry.remove(resource);
      if (segment != null)
      {
         segment.removeMatch(resource);
      }
      return segment;
   }

   public Segment add(String path, T resource)
   {
      if (path.startsWith("/")) path = path.substring(1);

      List<String> pathParamExpr = new ArrayList<String>();
      StringBuffer newPath = pullPathParamExpressions(path, pathParamExpr);
      path = newPath.toString();
      String[] segments = path.split("/");

      for (int i = 0; i < segments.length; i++)
      {
         segments[i] = putBackPathParamExpressions(segments[i], pathParamExpr);
      }
      Segment segment = addPath(segments, 0, resource);
      globalRegistry.put(resource, segment);
      return segment;
   }


   public List<T> match(String path) throws NotFoundException
   {
      int start = 0;
      return match(path, start);
   }

   public List<T> matchMulti(String path) throws NotFoundException
   {
      int start = 0;
      return matchMulti(path, start);
   }


}
