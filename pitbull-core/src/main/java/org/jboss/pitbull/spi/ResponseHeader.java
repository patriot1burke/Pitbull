package org.jboss.pitbull.spi;

/**
 * SPI to underlying subsystems holding of status and headers.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ResponseHeader
{
   StatusCode getStatusCode();

   OrderedHeaders getHeaders();
}
