package org.jboss.pitbull.client;

import java.io.IOException;

/**
 * Exception thrown if a failure happened when establishing a WebSocket connection.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HandshakeFailure extends IOException
{
   protected ClientResponse response;

   public HandshakeFailure()
   {
   }

   public HandshakeFailure(String s)
   {
      super(s);
   }

   public HandshakeFailure(String s, Throwable throwable)
   {
      super(s, throwable);
   }

   public HandshakeFailure(Throwable throwable)
   {
      super(throwable);
   }

   public HandshakeFailure(String s, ClientResponse response)
   {
      super(s);
      this.response = response;
   }

   public ClientResponse getResponse()
   {
      return response;
   }
}
