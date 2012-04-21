package org.jboss.pitbull.spi;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface StreamResponseWriter extends ResponseWriter
{

   /**
    * Called if the response has a body.  Will flush status, status message, and headers when OutputStream is first
    * written to.
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
