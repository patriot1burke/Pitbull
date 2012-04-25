package org.jboss.pitbull.spi;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ResponseWriter
{
   /**
    * Ends the request, flushing the status, statusMessage, and headers to client if it has not been called already.
    * This method can be called more than once, but will only execute flushes the first time.
    *
    * @param status
    * @param statusMessage
    * @param headers
    */
   void end(ResponseHeader responseHeader);

   /**
    * Was the response sent?
    *
    * @return
    */
   boolean isEnded();

}
