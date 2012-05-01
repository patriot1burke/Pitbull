package org.jboss.pitbull.initiators;

import org.jboss.pitbull.internal.logging.Logger;
import org.jboss.pitbull.spi.Connection;
import org.jboss.pitbull.spi.RequestHandler;
import org.jboss.pitbull.spi.RequestHeader;
import org.jboss.pitbull.spi.RequestInitiator;
import org.jboss.pitbull.spi.StatusCode;
import org.jboss.pitbull.spi.StreamHandler;
import org.jboss.pitbull.spi.StreamResponseWriter;

import java.io.IOException;
import java.io.InputStream;

/**
 * Helper class for basic stream handling
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class StreamedRequestInitiator implements RequestInitiator
{
   protected static final Logger logger = Logger.getLogger(StreamedRequestInitiator.class);

   public boolean canExecuteInWorkerThread()
   {
      return false;
   }

   public abstract void service(Connection connection, RequestHeader requestHeader, InputStream requestBody, StreamedResponse response) throws IOException;

   @Override
   public RequestHandler begin(final Connection connection, final RequestHeader requestHeader)
   {
      return new StreamHandler()
      {
         @Override
         public boolean canExecuteInWorkerThread()
         {
            return StreamedRequestInitiator.this.canExecuteInWorkerThread();
         }

         @Override
         public void execute(Connection connection, RequestHeader requestHeader, InputStream is, StreamResponseWriter writer)
         {
            StreamedResponse res = new StreamedResponse(writer);
            try
            {
               service(connection, requestHeader, is, res);
            }
            catch (Exception e)
            {
               if (!writer.isEnded() && (writer.getAllocatedStream() == null || !writer.getAllocatedStream().isCommitted()))
               {
                  logger.error("Failed to execute", e);
                  if (writer.getAllocatedStream() != null) writer.getAllocatedStream().reset();
                  res.getHeaders().clear();
                  res.setStatus(StatusCode.INTERNAL_SERVER_ERROR);
               }
               else
               {
                  throw new RuntimeException(e);
               }
            }
            writer.end(res.delegator);

         }

         @Override
         public void unsupportedHandler()
         {
         }
      };
   }
}
