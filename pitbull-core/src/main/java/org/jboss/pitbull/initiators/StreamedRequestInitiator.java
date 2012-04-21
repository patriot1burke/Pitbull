package org.jboss.pitbull.initiators;

import org.jboss.pitbull.spi.Connection;
import org.jboss.pitbull.spi.RequestHandler;
import org.jboss.pitbull.spi.RequestHeader;
import org.jboss.pitbull.spi.RequestInitiator;
import org.jboss.pitbull.spi.StreamHandler;
import org.jboss.pitbull.spi.StreamResponseWriter;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class StreamedRequestInitiator implements RequestInitiator
{
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
         protected InputStream is;
         protected StreamResponseWriter writer;

         @Override
         public void setInputStream(InputStream input)
         {
            is = input;
         }

         @Override
         public void setWriter(StreamResponseWriter writer)
         {
            this.writer = writer;
         }

         @Override
         public boolean canExecuteInWorkerThread()
         {
            return StreamedRequestInitiator.this.canExecuteInWorkerThread();
         }

         @Override
         public void execute(RequestHeader requestHeader)
         {
            StreamedResponse res = new StreamedResponse(writer);
            try
            {
               service(connection, requestHeader, is, res);
            }
            catch (Exception e)
            {
               if (writer.getAllocatedStream() == null || !writer.getAllocatedStream().isCommitted())
               {
                  res.getHeaders().clear();
                  res.setStatus(500);
                  res.setStatusMessage("Internal Server Error");
               }

               throw new RuntimeException(e);
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
