package org.jboss.pitbull.util.registry;

import org.jboss.pitbull.util.PathHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class ParentSegment<T> extends Segment<T>
{
   protected Map<String, SimpleSegment> simpleSegments = new HashMap<String, SimpleSegment>();
   protected Map<String, PathParamSegment> pathParamSegments = new HashMap<String, PathParamSegment>();
   protected List<PathParamSegment> sortedPathParamSegments = new ArrayList<PathParamSegment>();

   /**
    * returns a copied list of all child segments
    * @return
    */
   public List<Segment> getChildren()
   {
      List<Segment> children = new ArrayList<Segment>();
      children.addAll(simpleSegments.values());
      children.addAll(pathParamSegments.values());
      return children;
   }

   protected static StringBuffer pullPathParamExpressions(String path, List<String> pathParamExpr)
   {
      // Regular expressions can have '{' and '}' characters.  Replace them to do match
      path = PathHelper.replaceEnclosedCurlyBraces(path);

      Matcher matcher = PathHelper.URI_REGEX_PATTERN.matcher(path);
      StringBuffer newPath = new StringBuffer();
      while (matcher.find())
      {
         String regex = matcher.group(1);
         // Regular expressions can have '{' and '}' characters.  Recover original replacement
         pathParamExpr.add(PathHelper.recoverEnclosedCurlyBraces(regex));
         matcher.appendReplacement(newPath, "{x}");
      }
      matcher.appendTail(newPath);
      return newPath;
   }

   protected static String putBackPathParamExpressions(String path, List<String> pathParamExpr)
   {
      Matcher matcher = PathHelper.URI_REGEX_PATTERN.matcher(path);
      StringBuffer newPath = new StringBuffer();
      int index = 0;
      while (matcher.find())
      {
         String val = pathParamExpr.get(index++);
         // double encode slashes, so that slashes stay where they are
         val = val.replace("\\", "\\\\");
         matcher.appendReplacement(newPath, "{" + val + "}");
      }
      matcher.appendTail(newPath);
      return newPath.toString();
   }

   protected Segment addPath(String[] segments, int index, T resource)
   {
      String segment = segments[index];
      // Regular expressions can have '{' and '}' characters.  Replace them to do match
      String replacedCurlySegment = PathHelper.replaceEnclosedCurlyBraces(segment);
      Matcher withPathParam = PathHelper.URI_REGEX_PATTERN.matcher(replacedCurlySegment);
      if (withPathParam.find())
      {
         String expression = recombineSegments(segments, index);
         PathParamSegment segmentNode = pathParamSegments.get(expression);
         if (segmentNode == null)
         {
            segmentNode = new PathParamSegment(expression);
            pathParamSegments.put(segmentNode.getPathExpression(), segmentNode);
            sortedPathParamSegments.add(segmentNode);
            Collections.sort(sortedPathParamSegments);
            segmentNode.setParent(this);
         }
         segmentNode.addMatch(resource);
         return segmentNode;
      }
      else
      {
         SimpleSegment segmentNode = simpleSegments.get(segment);
         if (segmentNode == null)
         {
            segmentNode = new SimpleSegment(segment);
            segmentNode.setParent(this);
            simpleSegments.put(segment, segmentNode);
         }
         if (segments.length > index + 1)
         {
            return segmentNode.addPath(segments, index + 1, resource);
         }
         else
         {
            segmentNode.addMatch(resource);
            return segmentNode;
         }
      }

   }

   protected String recombineSegments(String[] segments, int index)
   {
      String expression = "";
      boolean first = true;
      for (int i = index; i < segments.length; i++)
      {
         if (first)
         {
            first = false;
         }
         else
         {
            expression += "/";
         }
         expression += segments[i];
      }
      return expression;
   }

   protected List<T> matchChildren(String path, int start)
   {
      String simpleSegment = null;
      if (start == path.length())
      {
         simpleSegment = "";
      }
      else
      {
         int endOfSegmentIndex = path.indexOf('/', start);
         if (endOfSegmentIndex > -1) simpleSegment = path.substring(start, endOfSegmentIndex);
         else simpleSegment = path.substring(start);
      }

      RegistryFailure lastFailure = null;

      SimpleSegment<T> segment = simpleSegments.get(simpleSegment);
      if (segment != null)
      {
         try
         {
            return segment.matchSimple(path, start);
         }
         catch (RegistryFailure e)
         {
            lastFailure = e;
         }
      }

      for (PathParamSegment<T> pathParamSegment : sortedPathParamSegments)
      {
         try
         {
            return pathParamSegment.matchPattern(path, start);
         }
         catch (RegistryFailure e)
         {
            // try and propagate matched path that threw non-404 responses, i.e. MethodNotAllowed, etc.
            if (lastFailure == null || lastFailure instanceof NotFoundException) lastFailure = e;
         }
      }
      if (lastFailure != null) throw lastFailure;
      throw new NotFoundException("Could not find resource for relative : " + path);
   }

   protected List<T> matchMultiChildren(String path, int start)
   {
      List<T> list = new ArrayList<T>();

      String simpleSegment = null;
      if (start == path.length())
      {
         simpleSegment = "";
      }
      else
      {
         int endOfSegmentIndex = path.indexOf('/', start);
         if (endOfSegmentIndex > -1) simpleSegment = path.substring(start, endOfSegmentIndex);
         else simpleSegment = path.substring(start);
      }

      RegistryFailure lastFailure = null;

      SimpleSegment<T> segment = simpleSegments.get(simpleSegment);
      if (segment != null)
      {
         try
         {
            list.addAll(segment.matchMulti(path, start));
         }
         catch (RegistryFailure e)
         {
            lastFailure = e;
         }
      }

      for (PathParamSegment<T> pathParamSegment : sortedPathParamSegments)
      {
         try
         {
            list.addAll(pathParamSegment.matchPattern(path, start));
         }
         catch (RegistryFailure e)
         {
            // try and propagate matched path that threw non-404 responses, i.e. MethodNotAllowed, etc.
            if (lastFailure == null || lastFailure instanceof NotFoundException) lastFailure = e;
         }
      }
      return list;
   }

   public List<T> matchMulti(String path, int start)
   {
      if (start < path.length() && path.charAt(start) == '/') start++;
      return matchMultiChildren(path, start);
   }

   public List<T> match(String path, int start)
   {
      if (start < path.length() && path.charAt(start) == '/') start++;
      return matchChildren(path, start);
   }
}
