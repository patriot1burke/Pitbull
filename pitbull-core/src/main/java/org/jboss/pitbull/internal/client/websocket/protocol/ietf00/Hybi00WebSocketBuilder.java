package org.jboss.pitbull.internal.client.websocket.protocol.ietf00;

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
import org.jboss.pitbull.internal.nio.websocket.impl.oio.internal.protocol.ietf00.Hybi00Handshake;
import org.jboss.pitbull.internal.nio.websocket.impl.oio.internal.util.Hash;
import org.jboss.pitbull.websocket.WebSocket;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import static org.jboss.pitbull.internal.nio.websocket.impl.oio.internal.WebSocketHeaders.*;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class Hybi00WebSocketBuilder extends WebSocketBuilder
{
   protected Random random = new Random();

   protected String key()
   {
      int spaces_1 = Hash.nextInt(12) + 1;
      int max_1 = Integer.MAX_VALUE / spaces_1;
      int number_1 = Hash.nextInt(max_1);
      int product_1 = number_1 * spaces_1;
      String key_1 = Integer.toString(product_1);
      int randomCharRange = 0x2F - 0x21 + 0x7E - 0x3A;

      int numChars = Hash.nextInt(12) + 1;
      StringBuilder builder = new StringBuilder(key_1);
      for (int i = 0; i < numChars; i++)
      {
         int index = Hash.nextInt(builder.length());
         int randomChar = Hash.nextInt(randomCharRange);
         char c = 0;
         if (randomChar < (0x2F - 0x21))
         {
            c = (char) (0x21 + randomChar);
         }
         else
         {
            c = (char) (0x3A + randomChar);
         }
         builder.insert(index, c);
      }
      for (int i = 0; i < spaces_1; i++)
      {
         int index = Hash.nextInt(builder.length());
         index--;
         if (index < 1) index = 1;
         builder.insert(index, ' ');
      }

      return builder.toString();
   }

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

         invocation.header("Upgrade", "WebSocket");
         invocation.header("Connection", "Upgrade");

         if (origin != null) invocation.header(SEC_WEBSOCKET_ORIGIN.getCanonicalHeaderName(), origin);
         if (protocol != null) invocation.header(SEC_WEBSOCKET_PROTOCOL.getCanonicalHeaderName(), protocol);


         String key_1 = key();
         invocation.header(SEC_WEBSOCKET_KEY1.getCanonicalHeaderName(), key_1);
         String key_2 = key();
         invocation.header(SEC_WEBSOCKET_KEY2.getCanonicalHeaderName(), key_2);
         byte[] key3 = new byte[8];
         Hash.getRandomBytes(key3);
         invocation.getRequestBody().write(key3);

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


         if (protocol != null)
         {
            String chosenProtocol = response.getHeaders().getFirstHeader(SEC_WEBSOCKET_PROTOCOL.getCanonicalHeaderName());
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


         if (origin != null)
         {
            String chosenOrigin = response.getHeaders().getFirstHeader(SEC_WEBSOCKET_ORIGIN.getCanonicalHeaderName());
            if (!origin.equals(chosenOrigin))
            {
               throw new HandshakeFailure("No origin match", response);
            }
         }

         BufferedBlockingInputStream inputStream = new BufferedBlockingInputStream(connection.getChannel(), response.getBuffer());
         Hybi00Handshake hybi00Handshake = new Hybi00Handshake();
         final byte[] sentSolution = Hybi00Handshake.solve(hybi00Handshake.getHashAlgorithm(), key_1, key_2, key3);
         final byte[] receivedSolution = new byte[16];
         for (int i = 0; i < 16; i++)
         {
            int b = inputStream.read();
            receivedSolution[i] = (byte) b;
         }

         if (!Arrays.equals(sentSolution, receivedSolution))
         {
            throw new HandshakeFailure("Keys don't match");
         }

         BufferedBlockingOutputStream outputStream = new BufferedBlockingOutputStream((connection.getChannel()));
         OioWebSocket oioWebSocket = hybi00Handshake.getClientWebSocket(
                 uri,
                 inputStream,
                 outputStream,
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
