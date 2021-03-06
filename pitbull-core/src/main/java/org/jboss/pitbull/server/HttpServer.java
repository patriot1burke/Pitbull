package org.jboss.pitbull.server;

import org.jboss.pitbull.internal.logging.Logger;
import org.jboss.pitbull.internal.nio.http.HttpConnector;
import org.jboss.pitbull.internal.nio.socket.Worker;
import org.jboss.pitbull.internal.util.registry.UriRegistry;
import org.jboss.pitbull.server.spi.RequestHandler;
import org.jboss.pitbull.server.spi.RequestInitiator;

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
   protected UriRegistry<Object> registry = new UriRegistry<Object>();
   protected static final Logger logger = Logger.getLogger(HttpServer.class);
   protected ExecutorService acceptorExecutor;
   protected ExecutorService requestExecutor;
   protected ExecutorService workerExecutor;

   protected Worker[] workers;
   protected int numWorkers;
   protected List<HttpConnector> connectors = new ArrayList<HttpConnector>();

   /**
    * Metrics of accepted connections
    *
    * @return
    */
   public long getAcceptCount()
   {
      long count = 0;
      for (HttpConnector connector : connectors)
      {
         count += connector.getAcceptCount();
      }
      return count;
   }

   /**
    * Metric of accepted connection distribution onto workers
    *
    * @return
    */
   public long[] getWorkerRegistrationDistribution()
   {
      long[] dist = new long[workers.length];
      for (int i = 0; i < dist.length; i++)
      {
         dist[i] = workers[i].getNumRegistered();
      }
      return dist;
   }

   /**
    * @return
    */
   public void clearMetrics()
   {
      for (HttpConnector connector : connectors)
      {
         connector.clearMetrics();
      }

      for (Worker worker : workers)
      {
         worker.clearMetrics();
      }

   }

   public List<HttpConnector> getConnectors()
   {
      return connectors;
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

   public void register(String mappingPattern, RequestHandler handler)
   {
      registry.register(mappingPattern, handler);
   }

   public void register(String mappingPattern, RequestInitiator initiator)
   {
      registry.register(mappingPattern, initiator);
   }

   public void unregister(RequestHandler handler)
   {
      registry.unregister(handler);
   }

   public void unregister(RequestInitiator initiator)
   {
      registry.unregister(initiator);
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
