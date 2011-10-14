package org.jboss.pitbull.spi;

import java.util.List;
import java.util.Map;

/**
 * SPI that allows transport to lazily parse headers received from the network buffer.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface RequestHeader
{
   String getMethod();
   String getUri();
   List<Map.Entry<String, String>> getHeaders();
}
