package org.jboss.pitbull.client.internal;

import org.jboss.pitbull.client.ClientConnection;
import org.jboss.pitbull.client.ClientInvocation;
import org.jboss.pitbull.client.ClientResponse;
import org.jboss.pitbull.handlers.PitbullChannel;
import org.jboss.pitbull.handlers.stream.ContentOutputStream;
import org.jboss.pitbull.internal.nio.http.HttpRequestHeader;
import org.jboss.pitbull.internal.nio.socket.Channels;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
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
   public ClientResponse response() throws IOException
   {
      getRequestBody().close();
      ClientResponseImpl impl = new ClientResponseImpl(connection);
      impl.awaitResponse();
      return impl;
   }

   @Override
   public Future<ClientResponse> asyncResponse()
   {
      return null;
   }
}
