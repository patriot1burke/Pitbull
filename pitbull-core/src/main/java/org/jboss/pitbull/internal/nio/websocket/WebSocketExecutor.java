package org.jboss.pitbull.internal.nio.websocket;

import org.jboss.pitbull.server.handlers.WebSocketHandler;
import org.jboss.pitbull.websocket.WebSocket;
import org.jboss.pitbull.internal.logging.Logger;
import org.jboss.pitbull.internal.nio.socket.BufferedBlockingInputStream;
import org.jboss.pitbull.internal.nio.socket.ManagedChannel;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class WebSocketExecutor implements Runnable
{
   protected WebSocket webSocket;
   protected ManagedChannel channel;
   protected WebSocketHandler handler;
   protected BufferedBlockingInputStream stream;
   protected static final Logger log = Logger.getLogger(WebSocketExecutor.class);

   public WebSocketExecutor(ManagedChannel channel, WebSocket webSocket, WebSocketHandler handler, BufferedBlockingInputStream stream)
   {
      this.channel = channel;
      this.webSocket = webSocket;
      this.handler = handler;
      this.stream = stream;
   }

   @Override
   public void run()
   {
      //long start = System.currentTimeMillis();
      //log.debug("Start Stream Executor");
      try
      {
         do
         {
            // loop while we still have stuff in read byffer.
            handler.onReceivedFrame(webSocket);
         } while (!webSocket.isClosed() && stream.bufferAvailable() > 0);
         if (!channel.isClosed())
         {
            channel.resumeReads();
         }
      }
      catch (Throwable ex)
      {
         try
         { webSocket.close(); }
         catch (Throwable t)
         {}
      }
      //log.debug("End Stream Executor: " + (System.currentTimeMillis() - start));
   }
}
