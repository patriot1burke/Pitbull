package org.jboss.pitbull.nio;

import org.jboss.pitbull.spi.RequestInitiator;
import org.jboss.pitbull.util.registry.UriRegistry;

import javax.net.ssl.SSLEngine;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HttpEventHandlerFactory implements EventHandlerFactory
{
   protected ExecutorService executor;
   protected SSLEngine ssl;
   protected UriRegistry<RequestInitiator> registry;

   public HttpEventHandlerFactory(ExecutorService executor, SSLEngine ssl, UriRegistry<RequestInitiator> registry)
   {
      this.executor = executor;
      this.ssl = ssl;
      this.registry = registry;
   }

   @Override
   public EventHandler create()
   {
      return new HttpEventHandler(executor, ssl, registry);
   }
}
