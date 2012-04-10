package org.jboss.pitbull;

import org.jboss.pitbull.internal.logging.Logger;
import org.jboss.pitbull.internal.nio.http.HttpConnector;
import org.jboss.pitbull.internal.nio.socket.Worker;
import org.jboss.pitbull.spi.RequestInitiator;
import org.jboss.pitbull.util.registry.UriRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HttpServer
{
   protected UriRegistry<RequestInitiator> registry = new UriRegistry<RequestInitiator>();
   protected static final Logger logger = Logger.getLogger(HttpServer.class);
   protected ExecutorService acceptorExecutor;
   protected ExecutorService requestExecutor;
   protected ExecutorService workerExecutor;

   protected Worker[] workers;
   protected int numWorkers;
   protected List<HttpConnector> connectors = new ArrayList<HttpConnector>();

   public List<HttpConnector> getConnectors()
   {
      return connectors;
   }

   public UriRegistry<RequestInitiator> getRegistry()
   {
      return registry;
   }

   public void setRegistry(UriRegistry<RequestInitiator> registry)
   {
      this.registry = registry;
   }

   public ExecutorService getAcceptorExecutor()
   {
      return acceptorExecutor;
   }

   public void setAcceptorExecutor(ExecutorService acceptorExecutor)
   {
      this.acceptorExecutor = acceptorExecutor;
   }

   public ExecutorService getRequestExecutor()
   {
      return requestExecutor;
   }

   public int getNumWorkers()
   {
      return numWorkers;
   }

   public void setNumWorkers(int numWorkers)
   {
      this.numWorkers = numWorkers;
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

   public void start() throws Exception
   {
      workers = new Worker[numWorkers];
      for (int i = 0; i < workers.length; i++)
      {
         workers[i] = new Worker();
         workerExecutor.execute(workers[i]);
      }
      for (HttpConnector connector : connectors)
      {
         connector.setRegistry(registry);
         connector.start(workers, acceptorExecutor, requestExecutor);
      }

   }

   public void stop() throws Exception
   {
      for (HttpConnector connector : connectors)
      {
         connector.shutdownAcceptor();
      }
      acceptorExecutor.shutdown();
      boolean awaitedAcceptor = acceptorExecutor.awaitTermination(60, TimeUnit.SECONDS);
      logger.trace("Shutdown Acceptor Threads: {0}", awaitedAcceptor);
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
      for (HttpConnector connector : connectors)
      {
         connector.shutdownChannel();
      }
      logger.trace("Closed channels");
   }
}
