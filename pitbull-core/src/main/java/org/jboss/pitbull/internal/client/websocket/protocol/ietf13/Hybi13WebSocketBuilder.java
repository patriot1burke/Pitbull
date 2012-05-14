package org.jboss.pitbull.internal.client.websocket.protocol.ietf13;

import org.jboss.pitbull.StatusCode;
import org.jboss.pitbull.client.ClientInvocation;
import org.jboss.pitbull.client.HandshakeFailure;
import org.jboss.pitbull.client.HttpConnectionFactory;
import org.jboss.pitbull.client.WebSocketBuilder;
import org.jboss.pitbull.internal.NotImplementedYetException;
import org.jboss.pitbull.internal.client.ClientConnectionImpl;
import org.jboss.pitbull.internal.client.ClientResponseImpl;
import org.jboss.pitbull.internal.nio.socket.BufferedBlockingInputStream;
import org.jboss.pitbull.internal.nio.socket.BufferedBlockingOutputStream;
import org.jboss.pitbull.internal.nio.websocket.WebSocketImpl;
import org.jboss.pitbull.internal.nio.websocket.impl.oio.ClosingStrategy;
import org.jboss.pitbull.internal.nio.websocket.impl.oio.OioWebSocket;
import org.jboss.pitbull.internal.nio.websocket.impl.oio.internal.protocol.ietf13.Hybi13Handshake;
import org.jboss.pitbull.internal.nio.websocket.impl.oio.internal.util.Base64;
import org.jboss.pitbull.websocket.WebSocket;
import org.jboss.pitbull.websocket.WebSocketVersion;

import java.io.IOException;
import java.util.Random;

import static org.jboss.pitbull.internal.nio.websocket.impl.oio.internal.WebSocketHeaders.*;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class Hybi13WebSocketBuilder extends WebSocketBuilder
{
   protected Random random = new Random();

   @Override
   protected WebSocket doConnect() throws IOException
   {
      final ClientConnectionImpl connection;

      if (secured)
      {
         if (trustStore == null) connection = (ClientConnectionImpl) HttpConnectionFactory.https(host, port);
         else throw new NotImplementedYetException("wss with truststore not implemented yet");
      }
      else
      {
         connection = (ClientConnectionImpl) HttpConnectionFactory.http(host, port);
      }
      try
      {
         ClientInvocation invocation = connection.request(path).get();

         invocation.header("Upgrade", "websocket");
         invocation.header("Connection", "upgrade");

         byte[] key = new byte[16];
         random.nextBytes(key);
         String encodedKey = Base64.encodeBase64String(key);
         invocation.header(SEC_WEBSOCKET_KEY.getCanonicalHeaderName(), encodedKey);

         if (origin != null) invocation.header(ORIGIN.getCanonicalHeaderName(), origin);

         invocation.header(SEC_WEBSOCKET_VERSION.getCanonicalHeaderName(), WebSocketVersion.HYBI_13.getCode());

         if (protocol != null) invocation.header(SEC_WEBSOCKET_PROTOCOL.getCanonicalHeaderName(), protocol);

         ClientResponseImpl response = (ClientResponseImpl) invocation.invoke();

         if (response.getStatus() != StatusCode.SWITCHING_PROTOCOLS)
         {
            throw new HandshakeFailure("Error making handshake: " + response.getStatus(), response);
         }

         String upgrade = response.getHeaders().getFirstHeader("Upgrade");
         if (!"websocket".equalsIgnoreCase(upgrade))
         {
            throw new HandshakeFailure("Incorrect Upgrade heaader", response);
         }

         String accept = response.getHeaders().getFirstHeader(SEC_WEBSOCKET_ACCEPT.getCanonicalHeaderName());
         if (accept == null)
         {
            throw new HandshakeFailure("No value for header: " + SEC_WEBSOCKET_ACCEPT.getCanonicalHeaderName());
         }

         Hybi13Handshake handshake = new Hybi13Handshake();

         String solution = handshake.solve(encodedKey);

         if (!solution.equals(accept.trim()))
         {
            throw new HandshakeFailure(SEC_WEBSOCKET_ACCEPT.getCanonicalHeaderName() + " does not meet expect value", response);
         }

         String chosenProtocol = response.getHeaders().getFirstHeader(SEC_WEBSOCKET_PROTOCOL.getCanonicalHeaderName());

         if (protocol != null)
         {
            boolean match = false;
            String[] split = protocol.split(",");
            for (String p : split)
            {
               if (p.equals(chosenProtocol))
               {
                  match = true;
                  break;
               }
            }
            if (!match)
            {
               throw new HandshakeFailure("No protocol match", response);
            }
         }

         OioWebSocket oioWebSocket = handshake.getClientWebSocket(
                 uri,
                 new BufferedBlockingInputStream(connection.getChannel(), response.getBuffer()),
                 new BufferedBlockingOutputStream((connection.getChannel())),
                 new ClosingStrategy()
                 {
                    @Override
                    public void doClose() throws IOException
                    {
                       connection.close();
                    }
                 }
         );

         return new WebSocketImpl(connection, oioWebSocket);
      }
      catch (IOException e)
      {
         connection.close();
         throw e;
      }
      catch (RuntimeException e)
      {
         connection.close();
         throw e;
      }
   }
}
