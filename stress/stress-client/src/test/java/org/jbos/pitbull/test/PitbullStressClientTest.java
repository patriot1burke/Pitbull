package org.jbos.pitbull.test;

import org.jboss.pitbull.HttpServer;
import org.jboss.pitbull.HttpServerBuilder;
import org.jboss.pitbull.stress.StressClient;
import org.jboss.pitbull.stress.StressService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PitbullStressClientTest
{
   public static HttpServer http;

   @BeforeClass
   public static void startup() throws Exception
   {
      http = new HttpServerBuilder().connector().add()
              .workers(2)
              .maxRequestThreads(4).build();
      http.start();
      http.getRegistry().add("/{.*}", new StressService());
   }

   @AfterClass
   public static void shutdown() throws Exception
   {
      http.stop();
   }

   @Test
   public void testClientStressMultiple() throws Exception
   {
      System.out.println("************************");
      System.out.println(" Vanilla Socket Stress");
      System.out.println("************************");
      for (int i = 5; i < 21; i += 5)
      {
         System.out.println();
         System.out.println();
         System.out.println("-- Test with client thread num: " + (i + 1) * 3);
         StressClient.stress(i + 1, 5);
         System.out.println("Account Count: " + http.getAcceptCount());
         System.out.print("Worker distribution: ");
         long[] dist = http.getWorkerRegistrationDistribution();
         for (int j = 0; j < dist.length; j++)
         {
            System.out.print(dist[j] + ", ");
         }
         System.out.println();
         http.clearMetrics();
      }
   }

}
