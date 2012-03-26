package org.jboss.pitbull.nio.http;

import org.jboss.pitbull.logging.Logger;
import org.jboss.pitbull.spi.RequestInitiator;
import org.jboss.pitbull.util.registry.UriRegistry;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.security.KeyStore;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PitbullServer
{
   protected int port = -1;
   protected int sslPort = -1;
   protected KeyStore keyStore;
   protected String root = "";
   protected UriRegistry<RequestInitiator> registry = new UriRegistry<RequestInitiator>();
   protected int numWorkers = 5;
   protected int numExecutors = 5;
   protected static final Logger logger = Logger.getLogger(PitbullServer.class);
   protected HttpEndpoint http;
   protected HttpEndpoint https;

   public UriRegistry<RequestInitiator> getRegistry()
   {
      return registry;
   }

   public KeyStore getKeyStore()
   {
      return keyStore;
   }

   public void setKeyStore(KeyStore keyStore)
   {
      this.keyStore = keyStore;
   }

   public int getPort()
   {
      return port;
   }

   public void setPort(int port)
   {
      this.port = port;
   }

   public int getSslPort()
   {
      return sslPort;
   }

   public void setSslPort(int sslPort)
   {
      this.sslPort = sslPort;
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
      if (port > -1)
      {
         logger.info("**** PORT: " + port);
         http = new HttpEndpoint();
         http.setPort(port);
         http.setNumWorkers(numWorkers);
         http.setNumExecutors(numExecutors);
         http.setRegistry(registry);
         http.setRoot(root);
         http.start();
      }
      if (sslPort > -1)
      {
         logger.info("**** SSLPORT: " + sslPort);
         KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
         kmf.init(keyStore, null);

         // Initialize the SSLContext to work with our key managers.
         SSLContext serverContext = SSLContext.getInstance("TLS");
         serverContext.init(kmf.getKeyManagers(), null, null);
      }

   }

   public void stop() throws Exception
   {
      if (http != null)
      {
         http.stop();
      }
   }
}
