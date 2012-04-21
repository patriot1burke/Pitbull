package org.jboss.pitbull.spi;

import org.jboss.pitbull.util.CaseInsensitiveMap;

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

   OrderedHeaders getHeaders();
}
