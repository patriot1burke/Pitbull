package org.jboss.pitbull.client.internal;

import org.jboss.pitbull.client.ClientInvocation;
import org.jboss.pitbull.client.ClientResponse;
import org.jboss.pitbull.handlers.PitbullChannel;
import org.jboss.pitbull.internal.nio.http.HttpRequestHeader;

import java.io.OutputStream;
import java.util.concurrent.Future;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ClientInvocationImpl implements ClientInvocation
{
   protected PitbullChannel channel;
   protected String host;
   protected HttpRequestHeader requestHeader = new HttpRequestHeader();


   public ClientInvocationImpl(PitbullChannel channel, String host)
   {
      this.channel = channel;
      this.host = host;
      requestHeader.getHeaders().setHeader("Host", host);
   }

   @Override
   public ClientInvocation header(String name, String value)
   {
      requestHeader.getHeaders().addHeader(name, value);
      return this;
   }

   @Override
   public ClientInvocation get()
   {
      requestHeader.setMethod("GET");
      return this;
   }

   @Override
   public ClientInvocation put()
   {
      requestHeader.setMethod("PUT");
      return this;
   }

   @Override
   public ClientInvocation post()
   {
      requestHeader.setMethod("POST");
      return this;
   }

   @Override
   public ClientInvocation delete()
   {
      requestHeader.setMethod("DELETE");
      return this;
   }

   @Override
   public ClientInvocation method(String method)
   {
      requestHeader.setMethod(method);
      return this;
   }

   @Override
   public OutputStream getRequestBody()
   {
      return null;
   }

   @Override
   public OutputStream getRequestBody(int bufferSize)
   {
      return null;
   }

   @Override
   public ClientResponse response()
   {
      return null;
   }

   @Override
   public Future<ClientResponse> asyncResponse()
   {
      return null;
   }
}
