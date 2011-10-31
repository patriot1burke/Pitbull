package org.jboss.pitbull.nio;

import org.jboss.pitbull.spi.RequestHeader;
import org.jboss.pitbull.util.CaseInsensitiveMap;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HttpRequestHeader implements RequestHeader
{
   protected String httpVersion;
   protected String method;
   protected String uri;
   protected CaseInsensitiveMap<String> headers = new CaseInsensitiveMap<String>();
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

   public CaseInsensitiveMap<String> getHeaders()
   {
      return headers;
   }

   public void addHeader(final String name, final String value)
   {
      headers.add(name, value);
   }
}
