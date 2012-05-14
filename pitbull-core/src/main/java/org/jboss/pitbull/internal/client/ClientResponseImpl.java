package org.jboss.pitbull.internal.client;

import org.jboss.pitbull.OrderedHeaders;
import org.jboss.pitbull.StatusCode;
import org.jboss.pitbull.client.ClientResponse;
import org.jboss.pitbull.internal.nio.http.ContentInputStream;
import org.jboss.pitbull.internal.util.OrderedHeadersImpl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ClientResponseImpl implements ClientResponse
{
   protected ClientConnectionImpl connection;
   protected String httpVersion;
   protected StatusCode status;
   protected OrderedHeaders headers = new OrderedHeadersImpl();
   protected ByteBuffer buffer;
   protected InputStream is;
   protected boolean closed;

   public ClientResponseImpl(ClientConnectionImpl connection)
   {
      this.connection = connection;
   }


   public void awaitHttpResponse() throws IOException
   {
      pullResponse();
      is = ContentInputStream.create(connection.channel, buffer, headers);
   }

   public void pullResponse() throws IOException
   {
      connection.setLast(this);
      HttpResponseDecoder decoder = new HttpResponseDecoder(this);
      buffer = ByteBuffer.allocate(8192);
      do
      {
         //int read = Channels.readBlocking(connection.channel.getChannel(), buffer);
         int read = connection.channel.readBlocking(buffer);
         if (read == -1)
         {
            connection.close();
            throw new ClosedChannelException();
         }
         if (read > 0) buffer.flip();
      } while (decoder.process(buffer) == false);
   }

   public ByteBuffer getBuffer()
   {
      return buffer;
   }

   public void setStatus(StatusCode status)
   {
      this.status = status;
   }

   @Override
   public String getHttpVersion()
   {
      return httpVersion;
   }

   public void setHttpVersion(String httpVersion)
   {
      this.httpVersion = httpVersion;
   }

   @Override
   public StatusCode getStatus()
   {
      return status;
   }

   @Override
   public OrderedHeaders getHeaders()
   {
      return headers;
   }

   @Override
   public InputStream getResponseBody()
   {
      return is;
   }

   public boolean isClosed()
   {
      return closed;
   }

   public void close() throws IOException
   {
      if (closed) return;
      closed = true;
      if (is != null)
      {
         if (is instanceof ContentInputStream) ((ContentInputStream) is).eat();
         is.close();
      }

   }

   @Override
   public String toString()
   {
      return "ClientResponse{" +
              "status=" + status +
              '}';
   }
}
