package org.jboss.pitbull.initiators;

import org.jboss.pitbull.internal.logging.Logger;
import org.jboss.pitbull.spi.Connection;
import org.jboss.pitbull.spi.RequestHandler;
import org.jboss.pitbull.spi.RequestHeader;
import org.jboss.pitbull.spi.RequestInitiator;
import org.jboss.pitbull.spi.StatusCode;
import org.jboss.pitbull.spi.StreamHandler;
import org.jboss.pitbull.spi.StreamedResponse;

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

   public abstract void service(Connection connection, RequestHeader requestHeader, InputStream requestBody, StreamedResponse response) throws IOException;

   @Override
   public RequestHandler begin(final Connection connection, final RequestHeader requestHeader)
   {
      return new StreamHandler()
      {
         @Override
         public void execute(Connection connection, RequestHeader requestHeader, InputStream is, StreamedResponse writer)
         {
            try
            {
               service(connection, requestHeader, is, writer);
            }
            catch (Exception e)
            {
               if (!writer.isEnded() && !writer.isCommitted())
               {
                  logger.error("Failed to execute", e);
                  writer.reset();
                  writer.getHeaders().clear();
                  writer.setStatus(StatusCode.INTERNAL_SERVER_ERROR);
               }
               else
               {
                  throw new RuntimeException(e);
               }
            }
            writer.end();

         }

         @Override
         public void unsupportedHandler()
         {
         }
      };
   }
}
