package org.jboss.pitbull.spi;

import java.io.InputStream;

/**
 * This is an endpoint that is expecting to use blocking java.io streams to process an HTTP request.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface StreamHandler extends RequestHandler
{
   /**
    * Whether or not the request handler can be run within the reader thread.  There is no guarantee that PitBull
    * will run the request in the same thread as the worker thread.  If this method returns true, the reader will assume
    * that the operation can be executed quickly and that no blocking occurs.
    *
    * @return
    */
   boolean canExecuteInWorkerThread();

   void execute(Connection connection, RequestHeader requestHeader, InputStream requestStream, StreamResponseWriter writer);
}
