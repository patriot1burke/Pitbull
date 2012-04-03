package org.jboss.pitbull.nio.socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SSLChannelFactory extends ManagedChannelFactory
{
   protected SSLContext sslContext;

   public SSLChannelFactory(SSLContext sslContext, EventHandlerFactory factory)
   {
      super(factory);
      this.sslContext = sslContext;
   }

   @Override
   public ManagedChannel create(SocketChannel channel) throws Exception
   {
      SSLEngine engine = sslContext.createSSLEngine();
      engine.setUseClientMode(false);
      return new SSLManagedChannel(channel, eventHandlerFactory.create(), engine);
   }
}
