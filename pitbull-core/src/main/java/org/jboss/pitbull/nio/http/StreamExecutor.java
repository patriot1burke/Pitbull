package org.jboss.pitbull.nio.http;

import org.jboss.pitbull.logging.Logger;
import org.jboss.pitbull.nio.socket.ManagedChannel;
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
      long start = System.currentTimeMillis();
      log.debug("Start Stream Executor");
      try
      {
         ContentInputStream is = ContentInputStream.create(channel, buffer, requestHeader);
         NioStreamResponseWriter writer = new NioStreamResponseWriter(channel, requestHeader, is, true); // todo handle keepalive setting
         handler.setInputStream(is);
         handler.setWriter(writer);
         try
         {
            handler.execute(requestHeader);
         }
         finally
         {
            /*
            try
            {
               is.eat();  // eat the input stream
            }
            catch (IOException e)
            {
               log.warn("Exception while eating", e);
            }
            */
         }
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
         log.debug("End Stream Executor: " + (System.currentTimeMillis() - start));
      }
   }
}
