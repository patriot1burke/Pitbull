package org.jboss.pitbull.internal.client;

import org.jboss.pitbull.ContentOutputStream;
import org.jboss.pitbull.client.ClientInvocation;
import org.jboss.pitbull.client.ClientResponse;
import org.jboss.pitbull.internal.NotImplementedYetException;
import org.jboss.pitbull.internal.nio.http.HttpRequestHeader;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ClientInvocationImpl implements ClientInvocation
{
   protected ClientConnectionImpl connection;
   protected HttpRequestHeader requestHeader = new HttpRequestHeader();
   protected ClientContentOutputStream output;


   public ClientInvocationImpl(ClientConnectionImpl connection, String path)
   {
      this.connection = connection;
      requestHeader.getHeaders().setHeader("Host", connection.getHostHeader());
      requestHeader.setUri(path);
      requestHeader.setHttpVersion("HTTP/1.1");
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
   public ContentOutputStream getRequestBody()
   {
      if (output == null)
      {
         output = new ClientContentOutputStream(requestHeader, connection.channel, 8192);
      }
      return output;
   }

   @Override
   public ClientResponse invoke() throws IOException
   {
      getRequestBody().close();
      ClientResponseImpl impl = new ClientResponseImpl(connection);
      impl.awaitHttpResponse();
      return impl;
   }

   @Override
   public Future<ClientResponse> submit() throws IOException
   {
      throw new NotImplementedYetException();
   }
}
