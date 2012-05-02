package org.jboss.pitbull.spi;

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

   void end();

   void setStatus(StatusCode status);

   void reset();
}
