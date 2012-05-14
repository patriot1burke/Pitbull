package org.jboss.pitbull.client;

import org.jboss.pitbull.OrderedHeaders;
import org.jboss.pitbull.StatusCode;

import java.io.InputStream;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ClientResponse
{
   String getHttpVersion();

   StatusCode getStatus();

   OrderedHeaders getHeaders();

   InputStream getResponseBody();
}
