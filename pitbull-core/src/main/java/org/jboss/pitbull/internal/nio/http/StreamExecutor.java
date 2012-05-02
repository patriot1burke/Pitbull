package org.jboss.pitbull.internal.nio.http;

import org.jboss.pitbull.Connection;
import org.jboss.pitbull.StatusCode;
import org.jboss.pitbull.internal.logging.Logger;
import org.jboss.pitbull.internal.nio.socket.ManagedChannel;
import org.jboss.pitbull.spi.StreamRequestHandler;

import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class StreamExecutor implements Runnable
{
   protected ManagedChannel channel;
   protected StreamRequestHandler handler;
   protected ByteBuffer buffer;
   protected HttpRequestHeader requestHeader;
   protected Connection connection;
   protected static final Logger log = Logger.getLogger(StreamExecutor.class);

   public StreamExecutor(Connection connection, ManagedChannel channel, StreamRequestHandler handler, ByteBuffer buffer, HttpRequestHeader requestHeader)
   {
      this.connection = connection;
      this.channel = channel;
      this.handler = handler;
      this.buffer = buffer;
      this.requestHeader = requestHeader;
   }

   @Override
   public void run()
   {
      //long start = System.currentTimeMillis();
      //log.debug("Start Stream Executor");
      ContentInputStream is = ContentInputStream.create(channel, buffer, requestHeader);
      NioStreamedResponse writer = new NioStreamedResponse(channel, requestHeader, is); // todo handle keepalive setting
      try
      {
         handler.execute(connection, requestHeader, is, writer);
         if (writer.isDetached() == false)
         {
            writer.end();
         }
      }
      catch (Throwable ex)
      {
         log.error("Failed to execute", ex);
         if (!writer.isEnded() && !writer.isCommitted())
         {
            try
            {
               writer.reset();
               writer.setStatus(StatusCode.INTERNAL_SERVER_ERROR);
               writer.end();
            }
            catch (Exception e)
            {
               try
               { channel.close(); }
               catch (Throwable t)
               {}
            }
         }
         else
         {
            try
            { channel.close(); }
            catch (Throwable t)
            {}
         }
      }
      //log.debug("End Stream Executor: " + (System.currentTimeMillis() - start));
   }
}
