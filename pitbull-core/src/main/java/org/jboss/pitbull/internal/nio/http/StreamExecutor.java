package org.jboss.pitbull.internal.nio.http;

import org.jboss.pitbull.internal.logging.Logger;
import org.jboss.pitbull.internal.nio.socket.ManagedChannel;
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
   protected static final Logger log = Logger.getLogger(StreamExecutor.class);

   public StreamExecutor(ManagedChannel channel, StreamHandler handler, ByteBuffer buffer, HttpRequestHeader requestHeader)
   {
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
         NioStreamResponseWriter writer = new NioStreamResponseWriter(channel, requestHeader, is, true); // todo handle keepalive setting
         handler.setInputStream(is);
         handler.setWriter(writer);
         handler.execute(requestHeader);
         channel.resumeReads();
      }
      catch (Exception ex)
      {
         log.error("Failed", ex);
         try
         { channel.close(); }
         catch (Throwable t)
         {}
      }
      finally
      {
         //log.debug("End Stream Executor: " + (System.currentTimeMillis() - start));
      }
   }
}
