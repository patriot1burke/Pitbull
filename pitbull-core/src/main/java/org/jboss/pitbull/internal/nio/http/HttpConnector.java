package org.jboss.pitbull.internal.nio.http;

import org.jboss.pitbull.internal.logging.Logger;
import org.jboss.pitbull.internal.nio.socket.Acceptor;
import org.jboss.pitbull.internal.nio.socket.ManagedChannelFactory;
import org.jboss.pitbull.internal.nio.socket.SSLChannelFactory;
import org.jboss.pitbull.internal.nio.socket.Worker;
import org.jboss.pitbull.spi.RequestInitiator;
import org.jboss.pitbull.util.registry.UriRegistry;

import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.ExecutorService;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HttpConnector
{
   protected SSLContext sslContext;
   protected int port = -1;
   protected UriRegistry<RequestInitiator> registry;
   protected Acceptor acceptor;
   protected ServerSocketChannel channel;
   protected static final Logger logger = Logger.getLogger(HttpConnector.class);


   public SSLContext getSslContext()
   {
      return sslContext;
   }

   public void setSslContext(SSLContext sslContext)
   {
      this.sslContext = sslContext;
   }

   public UriRegistry<RequestInitiator> getRegistry()
   {
      return registry;
   }

   public void setRegistry(UriRegistry<RequestInitiator> registry)
   {
      this.registry = registry;
   }

   public int getPort()
   {
      return port;
   }

   public void setPort(int port)
   {
      this.port = port;
   }

   public long getAcceptCount()
   {
      return acceptor.getAcceptCount();
   }

   public void clearMetrics()
   {
      acceptor.clearMetrics();
   }



   public void start(Worker[] workers, ExecutorService acceptorExecutor, ExecutorService requestExecutor) throws Exception
   {
      ManagedChannelFactory factory = null;
      if (sslContext != null)
      {
         if (port == -1) port = 8443;
         factory = new SSLChannelFactory(sslContext, new HttpEventHandlerFactory(requestExecutor, registry));
      }
      else
      {
         if (port == -1) port = 8080;
         factory = new ManagedChannelFactory(new HttpEventHandlerFactory(requestExecutor, registry));
      }
      channel = ServerSocketChannel.open();
      channel.configureBlocking(false);
      channel.socket().bind(new InetSocketAddress(port));
      acceptor = new Acceptor(channel, factory, workers);
      acceptorExecutor.execute(acceptor);
   }

   public void shutdownAcceptor() throws Exception
   {
      if (acceptor != null) acceptor.shutdown();
   }

   public void shutdownChannel() throws Exception
   {
      if (channel != null) channel.close();
   }


}
