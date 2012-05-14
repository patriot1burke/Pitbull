package org.jboss.pitbull.internal.nio.websocket;

import org.jboss.pitbull.ResponseHeader;
import org.jboss.pitbull.internal.nio.http.HttpResponse;
import org.jboss.pitbull.internal.nio.socket.BufferedBlockingOutputStream;
import org.jboss.pitbull.internal.nio.socket.ManagedChannel;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HandshakeOutputStream extends BufferedBlockingOutputStream
{
   protected ResponseHeader responseHeader;

   public HandshakeOutputStream(ResponseHeader responseHeader, ManagedChannel channel, int size)
   {
      super(channel, size);
      this.responseHeader = responseHeader;
   }

   @Override
   protected void flushBuffer() throws IOException
   {
      if (!committed) writeResponseHeader();
      committed = true;
      super.flushBuffer();
   }

   private void writeResponseHeader() throws IOException
   {
      HttpResponse response = new HttpResponse(responseHeader);
      byte[] bytes = response.responseBytes();
      ByteBuffer tmp = ByteBuffer.wrap(bytes);
      writeMessage(tmp);
   }

}
