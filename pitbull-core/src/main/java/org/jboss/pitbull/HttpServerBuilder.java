package org.jboss.pitbull;

import org.jboss.pitbull.crypto.KeyTools;
import org.jboss.pitbull.internal.nio.http.HttpConnector;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HttpServerBuilder<T extends HttpServerBuilder, Z extends HttpServer>
{
   private int workers = 1;
   private int maxRequestThreads = 1;
   private HttpServer server = create();

   public class ConnectorBuilder
   {
      private int port = -1;
      private boolean enableHttps;
      private KeyStore keyStore;
      private String keyStorePassword;

      public ConnectorBuilder port(int port)
      {
         this.port = port;
         return this;
      }

      public ConnectorBuilder https()
      {
         enableHttps = true;
         return this;
      }

      public ConnectorBuilder https(KeyStore keyStore, String password)
      {
         enableHttps = true;
         this.keyStore = keyStore;
         return this;
      }

      public T add() throws Exception
      {
         HttpConnector connector = new HttpConnector();
         connector.setPort(port);
         if (enableHttps)
         {
            KeyManagerFactory kmf = null;
            if (keyStore == null)
            {
               try
               {
                  keyStore = KeyTools.generateKeyStore();
                  kmf = KeyManagerFactory.getInstance("SunX509");
                  keyStore = KeyTools.generateKeyStore();
                  kmf.init(keyStore, new char[]{'p', 'a', 's', 's', 'w', 'o', 'r', 'd'});
               }
               catch (Exception e)
               {
                  throw new RuntimeException(e);
               }
            }
            else
            {
               kmf = KeyManagerFactory.getInstance("SunX509");
               kmf.init(keyStore, keyStorePassword.toCharArray());
            }

            // Initialize the SSLContext to work with our key managers.
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);


            // Initialize the SSLContext to work with our key managers.
            SSLContext serverContext = SSLContext.getInstance("TLS");
            serverContext.init(kmf.getKeyManagers(), null, null);
            connector.setSslContext(serverContext);
         }
         server.getConnectors().add(connector);
         return (T) HttpServerBuilder.this;
      }
   }

   public ConnectorBuilder connector()
   {
      return new ConnectorBuilder();
   }

   public T workers(int workers)
   {
      this.workers = workers;
      return (T) this;
   }

   public T maxRequestThreads(int max)
   {
      this.maxRequestThreads = max;
      return (T) this;
   }

   protected HttpServer create()
   {
      return new HttpServer();
   }


   public Z build()
   {
      server.setNumWorkers(workers);
      ExecutorService acceptorExecutor = Executors.newCachedThreadPool();
      ExecutorService requestExecutor = Executors.newFixedThreadPool(maxRequestThreads);
      ExecutorService workerExecutor = Executors.newFixedThreadPool(workers);
      server.setAcceptorExecutor(acceptorExecutor);
      server.setRequestExecutor(requestExecutor);
      server.setWorkerExecutor(workerExecutor);
      return (Z) server;
   }
}
