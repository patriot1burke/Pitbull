package org.jboss.pitbull.internal.nio.websocket;

import org.jboss.pitbull.server.handlers.WebSocketHandler;
import org.jboss.pitbull.websocket.WebSocket;
import org.jboss.pitbull.internal.logging.Logger;
import org.jboss.pitbull.internal.nio.socket.BufferedBlockingInputStream;
import org.jboss.pitbull.internal.nio.socket.ManagedChannel;

import java.util.concurrent.ExecutorService;

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
   protected ExecutorService executorService;

   protected static final Logger log = Logger.getLogger(WebSocketExecutor.class);

   public WebSocketExecutor(ManagedChannel channel, WebSocket webSocket, WebSocketHandler handler, BufferedBlockingInputStream stream, ExecutorService executorService)
   {
      this.channel = channel;
      this.webSocket = webSocket;
      this.handler = handler;
      this.stream = stream;
      this.executorService = executorService;
   }

   @Override
   public void run()
   {
      //long start = System.currentTimeMillis();
      //log.debug("Start Stream Executor");
      try
      {
         handler.onReceivedFrame(webSocket);
         if (!webSocket.isClosed() && stream.bufferAvailable() > 0)
         {
            // queue up again, we want to be fair to other applications!
            executorService.execute(this);
         }
         else if (!channel.isClosed())
         {
            // there's nothing in buffer, so re-register with selector and wait for a read.
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
