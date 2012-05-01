package org.jboss.pitbull.internal.nio.http;

import org.jboss.pitbull.internal.logging.Logger;
import org.jboss.pitbull.internal.nio.socket.ManagedChannel;
import org.jboss.pitbull.spi.Connection;
import org.jboss.pitbull.spi.StreamHandler;

import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class StreamExecutor implements Runnable
{
   protected ManagedChannel channel;
   protected StreamHandler handler;
   protected ByteBuffer buffer;
   protected HttpRequestHeader requestHeader;
   protected Connection connection;
   protected static final Logger log = Logger.getLogger(StreamExecutor.class);

   public StreamExecutor(Connection connection, ManagedChannel channel, StreamHandler handler, ByteBuffer buffer, HttpRequestHeader requestHeader)
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
      try
      {
         ContentInputStream is = ContentInputStream.create(channel, buffer, requestHeader);
         NioStreamResponseWriter writer = new NioStreamResponseWriter(channel, requestHeader, is); // todo handle keepalive setting
         handler.execute(connection, requestHeader, is, writer);
      }
      catch (Exception ex)
      {
         log.error("Failed to execute", ex);
         try
         { channel.close(); }
         catch (Throwable t)
         {}
         return;
      }
      finally
      {
         //log.debug("End Stream Executor: " + (System.currentTimeMillis() - start));
      }

      // todo keep-alive logic, right now everything kept alive
      try
      {
         channel.resumeReads();
      }
      catch (Exception ex)
      {
         log.error("Failed to resumeReads", ex);
         try
         { channel.close(); }
         catch (Throwable t)
         {}
         return;
      }

   }
}
