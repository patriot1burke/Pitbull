package org.jboss.pitbull.internal.client;


import org.jboss.pitbull.OrderedHeaders;
import org.jboss.pitbull.StatusCode;
import org.jboss.pitbull.internal.nio.http.HttpMessageDecoder;

import java.nio.ByteBuffer;

/**
 * Class that will handle parsing an HTTP request
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HttpResponseDecoder extends HttpMessageDecoder
{
   protected ClientResponseImpl response;

   public HttpResponseDecoder(ClientResponseImpl response)
   {
      this.response = response;
   }

   @Override
   protected OrderedHeaders getHeaders()
   {
      return response.getHeaders();
   }

   @Override
   protected boolean readInitial(ByteBuffer buffer)
   {
      String line = readLine(buffer);
      if (line == null) return false;
      String[] split = splitInitialLine(line);
      if (split.length < 3)
      {
         currentState = States.SKIP_CONTROL_CHARS;
         return true;
      }
      String version = split[0].trim();
      response.setHttpVersion(version);
      int code = Integer.valueOf(split[1]);
      response.setStatus(StatusCode.create(code, split[2].trim()));
      currentState = States.READ_HEADERS;
      return true;
   }


   protected String[] splitInitialLine(String sb)
   {
      int aStart;
      int aEnd;
      int bStart;
      int bEnd;
      int cStart;
      int cEnd;

      aStart = findNonWhitespace(sb, 0);
      aEnd = findWhitespace(sb, aStart);

      bStart = findNonWhitespace(sb, aEnd);
      bEnd = findWhitespace(sb, bStart);

      cStart = findNonWhitespace(sb, bEnd);
      cEnd = findEndOfString(sb);

      return new String[]{
              sb.substring(aStart, aEnd),
              sb.substring(bStart, bEnd),
              cStart < cEnd ? sb.substring(cStart, cEnd) : ""};
   }


}
