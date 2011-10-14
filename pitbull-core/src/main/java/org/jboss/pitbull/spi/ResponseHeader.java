package org.jboss.pitbull.spi;

import java.util.List;
import java.util.Map;

/**
 * SPI to underlying subsystems holding of status and headers.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ResponseHeader
{
   int getStatus();
   String getStatusMessage();
   List<Map.Entry<String, String>> getHeaders();
}
