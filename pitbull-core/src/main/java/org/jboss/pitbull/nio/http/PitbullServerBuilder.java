package org.jboss.pitbull.nio.http;

import org.jboss.pitbull.crypto.KeyTools;

import java.security.KeyStore;
import java.util.concurrent.ExecutorService;

public class PitbullServerBuilder<T extends PitbullServerBuilder, Z extends PitbullServer>
{
   protected int port = 8080;
   protected int sslPort = 8443;
   protected KeyStore keyStore;
   protected int numWorkers;
   protected int numExecutors;
   protected boolean enableHttps = false;
   protected boolean secured = false;

   public T port(int port)
   {
      this.port = port;
      return (T)this;
   }

   public T secured()
   {
      this.secured = true;
      return (T)this;
   }

   public T enableHttps()
   {
      enableHttps = true;
      return (T)this;
   }

   public T enableHttps(KeyStore keyStore)
   {
      enableHttps = true;
      this.keyStore = keyStore;
      return (T)this;
   }

   public T numWorkers(int numWorkers)
   {
      this.numWorkers = numWorkers;
      return (T)this;
   }

   public T numExecutors(int numExecutors)
   {
      this.numExecutors = numExecutors;
      return (T)this;
   }

   protected PitbullServer create()
   {
     return new PitbullServer();
   }

   public Z build()
   {
      PitbullServer server = create();
      if (enableHttps && keyStore == null)
      {
         try
         {
            keyStore = KeyTools.generateKeyStore();
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
      if (!secured) server.setPort(port);
      if (enableHttps)
      {
         server.setSslPort(sslPort);
         server.setKeyStore(keyStore);
      }
      server.setNumWorkers(numWorkers);
      server.setNumExecutors(numExecutors);
      return (Z)server;
   }
}