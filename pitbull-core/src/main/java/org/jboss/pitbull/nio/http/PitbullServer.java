package org.jboss.pitbull.nio.http;

import org.jboss.pitbull.nio.socket.Acceptor;
import org.jboss.pitbull.nio.socket.Worker;
import org.jboss.pitbull.spi.RequestInitiator;
import org.jboss.pitbull.util.registry.UriRegistry;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PitbullServer
{
   protected int port = 8080;
   protected String root = "";
   protected UriRegistry<RequestInitiator> registry = new UriRegistry<RequestInitiator>();
   protected ExecutorService requestExecutor;
   protected ExecutorService workerExecutor;
   protected Acceptor acceptor;
   protected Worker[] workers;
   protected int numWorkers = 5;
   protected int numExecutors = 5;
   protected ServerSocketChannel channel;

   public UriRegistry<RequestInitiator> getRegistry()
   {
      return registry;
   }

   public int getPort()
   {
      return port;
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
      requestExecutor = Executors.newFixedThreadPool(numExecutors);
      workerExecutor = Executors.newFixedThreadPool(numWorkers + 1);
      workers = new Worker[numWorkers];
      for (int i = 0; i < workers.length; i++)
      {
         workers[i] = new Worker(new HttpEventHandlerFactory(requestExecutor, null, registry));
         workerExecutor.execute(workers[i]);
      }
      channel = ServerSocketChannel.open();
      channel.configureBlocking(false);
      channel.socket().bind(new InetSocketAddress(port));
      acceptor = new Acceptor(channel, workers);
      workerExecutor.execute(acceptor);
   }

   public void stop() throws Exception
   {
      acceptor.shutdown();
      for (Worker worker : workers)
      {
         worker.shutdown();
      }
      requestExecutor.shutdown();
      try
      {
         requestExecutor.awaitTermination(60, TimeUnit.SECONDS);
      }
      catch (InterruptedException ignore)
      {

      }
      for (Worker worker : workers)
      {
         worker.close();
      }
      channel.close();


   }
}
