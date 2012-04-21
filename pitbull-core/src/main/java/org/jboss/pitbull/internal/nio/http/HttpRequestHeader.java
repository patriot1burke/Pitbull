package org.jboss.pitbull.internal.nio.http;

import org.jboss.pitbull.spi.OrderedHeaders;
import org.jboss.pitbull.spi.RequestHeader;
import org.jboss.pitbull.util.CaseInsensitiveMap;
import org.jboss.pitbull.util.OrderedHeadersImpl;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HttpRequestHeader implements RequestHeader
{
   protected String httpVersion;
   protected String method;
   protected String uri;
   protected OrderedHeaders headers = new OrderedHeadersImpl();
   protected boolean chunked;

   public String getHttpVersion()
   {
      return httpVersion;
   }

   public void setHttpVersion(String httpVersion)
   {
      this.httpVersion = httpVersion;
   }

   public String getMethod()
   {
      return method;
   }

   public void setMethod(String method)
   {
      this.method = method;
   }

   public String getUri()
   {
      return uri;
   }

   public void setUri(String uri)
   {
      this.uri = uri;
   }

   public OrderedHeaders getHeaders()
   {
      return headers;
   }

   @Override
   public String toString()
   {
      return "HttpRequestHeader{" +
              "httpVersion='" + httpVersion + '\'' +
              ", method='" + method + '\'' +
              ", uri='" + uri + '\'' +
              '}';
   }
}
