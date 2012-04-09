package org.jboss.pitbull.nio.http;

import org.jboss.pitbull.logging.Logger;
import org.jboss.pitbull.nio.socket.Acceptor;
import org.jboss.pitbull.nio.socket.ExecutorThreadFactory;
import org.jboss.pitbull.nio.socket.ManagedChannelFactory;
import org.jboss.pitbull.nio.socket.SSLChannelFactory;
import org.jboss.pitbull.nio.socket.Worker;
import org.jboss.pitbull.spi.RequestInitiator;
import org.jboss.pitbull.util.registry.UriRegistry;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HttpEndpoint
{
   protected SSLContext sslContext;
   protected int port = 8080;
   protected String root = "";
   protected UriRegistry<RequestInitiator> registry;
   protected ExecutorService requestExecutor;
   protected ExecutorService workerExecutor;
   protected Acceptor acceptor;
   protected Worker[] workers;
   protected int numWorkers = 5;
   protected int numExecutors = 5;
   protected ServerSocketChannel channel;
   protected static final Logger logger = Logger.getLogger(HttpEndpoint.class);


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

   public int getNumWorkers()
   {
      return numWorkers;
   }

   public void setNumWorkers(int numWorkers)
   {
      this.numWorkers = numWorkers;
   }

   public int getNumExecutors()
   {
      return numExecutors;
   }

   public void setNumExecutors(int numExecutors)
   {
      this.numExecutors = numExecutors;
   }

   public ExecutorService getRequestExecutor()
   {
      return requestExecutor;
   }

   public void setRequestExecutor(ExecutorService requestExecutor)
   {
      this.requestExecutor = requestExecutor;
   }

   public ExecutorService getWorkerExecutor()
   {
      return workerExecutor;
   }

   public void setWorkerExecutor(ExecutorService workerExecutor)
   {
      this.workerExecutor = workerExecutor;
   }

   public String getRoot()
   {
      return root;
   }

   public void setRoot(String root)
   {
      this.root = root;
   }

   public void start() throws Exception
   {

      if (requestExecutor == null)
      {
         requestExecutor = Executors.newFixedThreadPool(numExecutors, ExecutorThreadFactory.singleton);
      }
      if (workerExecutor == null)
      {
         workerExecutor = Executors.newFixedThreadPool(numWorkers + 1);
      }
      workers = new Worker[numWorkers];
      for (int i = 0; i < workers.length; i++)
      {
         workers[i] = new Worker();
         workerExecutor.execute(workers[i]);
      }
      channel = ServerSocketChannel.open();
      channel.configureBlocking(false);
      channel.socket().bind(new InetSocketAddress(port));
      ManagedChannelFactory factory = null;
      if (sslContext != null)
      {
         factory = new SSLChannelFactory(sslContext, new HttpEventHandlerFactory(requestExecutor, registry));
      }
      else
      {
         factory = new ManagedChannelFactory(new HttpEventHandlerFactory(requestExecutor, registry));
      }
      acceptor = new Acceptor(channel, factory, workers);
      workerExecutor.execute(acceptor);
   }

   public void stop() throws Exception
   {
      acceptor.shutdown();
      logger.trace("Shutdown Acceptor Thread");
      for (Worker worker : workers)
      {
         worker.shutdown();
      }
      logger.trace("Shutdown Workers");
      workerExecutor.shutdown();
      boolean awaitedWorker = workerExecutor.awaitTermination(60, TimeUnit.SECONDS);
      logger.trace("Shutdown Worker Executor: {0}", awaitedWorker);
      requestExecutor.shutdown();
      boolean awaited = requestExecutor.awaitTermination(60, TimeUnit.SECONDS);
      logger.trace("Shutdown Request Executor: {0}", awaited);
      if (awaited == false) requestExecutor.shutdownNow();
      for (Worker worker : workers)
      {
         worker.close();
      }
      logger.trace("Closed all workers");
      channel.close();
      logger.trace("Closed channel");


   }
}
