package org.jboss.pitbull.internal.client;


import org.jboss.pitbull.internal.nio.socket.SSLChannel;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ClientSSLChannel extends SSLChannel
{
   public ClientSSLChannel(SocketChannel channel, SSLEngine engine) throws Exception
   {
      super(channel, engine);
      doHandshake();
   }

   public void doHandshake() throws IOException
   {
      log.trace("client doHandshake()");
      inputBuffer.clear();
      appBuffer.clear();
      SSLEngineResult res;
      try
      {
         for (; ; )
         {
            handshakeStatus = engine.getHandshakeStatus();
            switch (handshakeStatus)
            {
               case FINISHED:
                  log.trace("client Handshake FINISHED");
                  return;
               case NEED_TASK:
                  log.trace("client Handshake NEED_TASK");
                  executeEngineTasks();
                  break;
               case NEED_UNWRAP:
                  log.trace("client Handshake NEED_UNWRAP");
                  if (inputBuffer.position() > 0
                          && engineStatus != SSLEngineResult.Status.BUFFER_UNDERFLOW)
                  {
                     log.trace("Calling needUnwrap");
                     if (needUnwrap())
                     {
                        throw new IOException("Socket closed");
                     }
                  }
                  else
                  {
                     log.trace("client unwrap readBlockingSuper");
                     int read = this.readBlockingSuper(inputBuffer);
                     if (read == -1)
                     {
                        log.trace("client aborting");
                        throw new IOException("Socket closed");
                     }
                     else
                     {
                        log.trace("read {0}, inputBuffer.position() {1}", read, inputBuffer.position());
                        if (needUnwrap())
                        {
                           throw new IOException("Socket closed");
                        }
                     }
                  }
                  break;
               case NEED_WRAP:
                  log.trace("client Handshake NEED_WRAP");
                  outputBuffer.clear();
                  res = engine.wrap(dummy, outputBuffer);
                  handshakeStatus = res.getHandshakeStatus();
                  outputBuffer.flip();
                  if (super.writeBlockingSuper(outputBuffer) == -1)
                  {
                     throw new IOException("Error writing");
                  }
                  break;
               case NOT_HANDSHAKING:
                  log.trace("client Handshake NOT_HANDSHAKING");
                  return;
            }
         }
      }
      finally
      {
         appBuffer.flip();
         handshakeStatus = engine.getHandshakeStatus();
         log.trace("End doHandshake() : {0}", handshakeStatus);
      }

   }
}
