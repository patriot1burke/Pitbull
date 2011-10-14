package org.jboss.pitbull.netty;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.pitbull.spi.RequestHeader;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class NettyRequestHeader implements RequestHeader
{
   protected final HttpRequest request;

   public NettyRequestHeader(HttpRequest request)
   {
      this.request = request;
   }

   public HttpRequest getRequest()
   {
      return request;
   }

   @Override
   public String getMethod()
   {
      return request.getMethod().getName();
   }

   @Override
   public String getUri()
   {
      return request.getUri();
   }

   @Override
   public List<Map.Entry<String, String>> getHeaders()
   {
      return request.getHeaders();
   }
}
