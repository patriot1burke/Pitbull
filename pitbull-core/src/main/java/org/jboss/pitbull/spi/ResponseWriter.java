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
    * Ends the request, flushing the status, statusMessage, and headers to client.
    *
    * @param status
    * @param statusMessage
    * @param headers
    */
   void end(ResponseHeader responseHeader);

}
