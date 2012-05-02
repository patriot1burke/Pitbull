package org.jboss.pitbull.spi;

/**
 * RequestInitiators will return instances of RequestHanders.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface RequestHandler
{
   /**
    * Callback to tell handler that its type is not supported by PitBull
    */
   void unsupportedHandler();
}
