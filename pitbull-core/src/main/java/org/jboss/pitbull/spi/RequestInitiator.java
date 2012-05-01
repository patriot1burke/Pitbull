package org.jboss.pitbull.spi;

/**
 * Responsible for initiating request processing with the container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface RequestInitiator
{
   /**
    * Called when a new HTTP request is available for processing.  Container returns a RequestHandler that implements
    * one or more RequestHandler sub-interfaces.
    *
    * @param connection
    * @param header     method, uri, and request headers
    * @return
    */
   RequestHandler begin(Connection connection, RequestHeader requestHeader);
}
