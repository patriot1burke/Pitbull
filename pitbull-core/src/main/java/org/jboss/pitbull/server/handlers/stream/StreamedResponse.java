package org.jboss.pitbull.server.handlers.stream;

import org.jboss.pitbull.ContentOutputStream;
import org.jboss.pitbull.ResponseHeader;
import org.jboss.pitbull.StatusCode;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface StreamedResponse extends ResponseHeader
{

   /**
    * Status and response headers are flushed first time OutputStream is written to.
    *
    * @return
    */
   ContentOutputStream getOutputStream();

   boolean isCommitted();

   boolean isEnded();

   /**
    * Flushes status code, response headers, and OutputStream if they haven't been flushed already.
    * <p/>
    * If the connection is keepalive, then control of underlying channel is returned to PitBull.  Connection may
    * be closed automatically if an internal error condition is met.
    */
   void end();

   void setStatus(StatusCode status);

   /**
    * Convenience function that resets the stream and clears all headers.
    *
    * @throws IllegalArgumentException if stream is committed
    */
   void reset() throws IllegalArgumentException;

   /**
    * Whether or not the response is managed by Pitbull
    *
    * @return
    */
   boolean isDetached();

   /**
    * Calling this method will detach
    * the StreamedResponse and it will -not- be automatically ended when the RequestHandler returns.
    */
   void detach();
}
