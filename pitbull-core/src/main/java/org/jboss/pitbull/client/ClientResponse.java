package org.jboss.pitbull.client;

import org.jboss.pitbull.ResponseHeader;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ClientResponse
{
   ResponseHeader getResponseHeader();
   InputStream getResponseBody();
}
