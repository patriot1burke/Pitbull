package org.jboss.pitbull.internal.nio.http;

import org.jboss.pitbull.internal.nio.socket.EventHandler;
import org.jboss.pitbull.internal.nio.socket.EventHandlerFactory;
import org.jboss.pitbull.spi.RequestInitiator;
import org.jboss.pitbull.util.registry.UriRegistry;

import java.util.concurrent.ExecutorService;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HttpEventHandlerFactory implements EventHandlerFactory
{
   protected ExecutorService executor;
   protected UriRegistry<RequestInitiator> registry;

   public HttpEventHandlerFactory(ExecutorService executor, UriRegistry<RequestInitiator> registry)
   {
      this.executor = executor;
      this.registry = registry;
   }

   @Override
   public EventHandler create()
   {
      return new HttpEventHandler(executor, registry);
   }
}
