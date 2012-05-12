package org.jboss.pitbull.internal.util.registry;

import org.jboss.pitbull.internal.util.PathHelper;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PathParamSegment<T> extends Segment<T> implements Comparable<PathParamSegment>
{
   protected String pathExpression;
   protected String regex;
   protected Pattern pattern;
   protected int literalCharacters;
   protected int numGroups;

   public int compareTo(PathParamSegment pathParamSegment)
   {
      // as per spec sort first by literal characters, then numCapturing groups, then num non-default groups

      if (literalCharacters > pathParamSegment.literalCharacters) return -1;
      if (literalCharacters < pathParamSegment.literalCharacters) return 1;

      if (numGroups > pathParamSegment.numGroups) return -1;
      if (numGroups < pathParamSegment.numGroups) return 1;

      return 0;
   }

   @Override
   protected void removeSegment()
   {
      parent.pathParamSegments.remove(getPathExpression());
      parent.sortedPathParamSegments.remove(this);
   }

   public PathParamSegment(String segment)
   {
      this.pathExpression = segment;
      String replacedCurlySegment = PathHelper.replaceEnclosedCurlyBraces(segment);
      literalCharacters = PathHelper.URI_REGEX_PATTERN.matcher(replacedCurlySegment).replaceAll("").length();

      String[] split = PathHelper.URI_REGEX_PATTERN.split(replacedCurlySegment);
      Matcher withPathParam = PathHelper.URI_REGEX_PATTERN.matcher(replacedCurlySegment);
      int i = 0;
      StringBuffer buffer = new StringBuffer();
      if (i < split.length) buffer.append(Pattern.quote(split[i++]));
      int groupNumber = 1;

      while (withPathParam.find())
      {
         buffer.append("(");
         String expr = withPathParam.group(1);
         expr = PathHelper.recoverEnclosedCurlyBraces(expr);
         buffer.append(expr);
         numGroups++;
         buffer.append(")");
         if (i < split.length) buffer.append(Pattern.quote(split[i++]));
      }
      regex = buffer.toString();
      pattern = Pattern.compile(regex);
   }

   public String getRegex()
   {
      return regex;
   }

   public String getPathExpression()
   {
      return pathExpression;
   }

   protected List<T> matchPattern(String path, int start)
   {
      Matcher matcher = pattern.matcher(path);
      matcher.region(start, path.length());

      if (matcher.matches())
      {
         if (matches == null || matches.size() < 1)
            throw new NotFoundException("Could not find resource for relative : " + path);
         return matches;
      }
      throw new NotFoundException("Could not find resource for relative : " + path);
   }

}
