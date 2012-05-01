package org.jboss.pitbull.spi;

/**
 * RequestProcessors will return instances of RequestHandler.  The instance's class will implement one or more
 * handlers.  For example, if the system wants an input stream and a zero copy response, then it would implement those
 * two corresponding handlers.
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
