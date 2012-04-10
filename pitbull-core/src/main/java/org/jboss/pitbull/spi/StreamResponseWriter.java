package org.jboss.pitbull.spi;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface StreamResponseWriter extends ResponseWriter
{

   /**
    * Called if the response has a body.  Flushes status, status message, and headers to the client.
    * Returns an OutputStream so that you can write the response body.
    *
    * @param status
    * @param statusMessage
    * @param headers
    * @return
    */
   ContentOutputStream getStream(ResponseHeader responseHeader);

   /**
    *
    * @return null of getStream() was never called
    */
   ContentOutputStream getAllocatedStream();
}
