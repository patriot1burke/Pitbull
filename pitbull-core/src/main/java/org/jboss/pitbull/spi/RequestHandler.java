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
    * Whether or not the request handler can be run within the reader thread.  There is no guarantee that PitBull
    * will run the request in the same thread as the reader.  If this method returns true, the reader will assume
    * that the operation can be executed quickly and that no blocking occurs.
    *
    * @return
    */
   boolean canExecuteInWorkerThread();

   void execute(RequestHeader requestHeader);

   /**
    * Callback to tell handler that its type is not supported by PitBull
    */
   void unsupportedHandler();
}
