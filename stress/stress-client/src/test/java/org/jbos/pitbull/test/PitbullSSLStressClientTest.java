package org.jbos.pitbull.test;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.jboss.pitbull.HttpServer;
import org.jboss.pitbull.HttpServerBuilder;
import org.jboss.pitbull.stress.StressClient;
import org.jboss.pitbull.stress.StressService;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PitbullSSLStressClientTest
{
   public static HttpServer http;
   public static StressClient.ExecutorFactory factory;


   @BeforeClass
   public static void startup() throws Exception
   {
      http = new HttpServerBuilder().connector().https().add()
              .workers(2)
              .maxRequestThreads(4).build();
      http.start();
      http.getRegistry().add("/{.*}", new StressService());
      factory = new StressClient.ExecutorFactory()
      {
         @Override
         public String getUrl()
         {
            return "https://localhost:8443/echo";
         }

         @Override
         public ApacheHttpClient4Executor create()
         {

            return new ApacheHttpClient4Executor(createHttpClient(8443));
         }
      };
   }

   @AfterClass
   public static void shutdown() throws Exception
   {
      http.stop();
   }

   private static DefaultHttpClient createHttpClient(int port)
   {
      try
      {
         java.lang.System.setProperty(
                 "sun.security.ssl.allowUnsafeRenegotiation", "true");

         // First create a trust manager that won't care.
         X509TrustManager trustManager = new X509TrustManager()
         {
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException
            {
               // Don't do anything.
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException
            {
               // Don't do anything.
            }

            public X509Certificate[] getAcceptedIssuers()
            {
               // Don't do anything.
               return null;
            }
         };

         // Now put the trust manager into an SSLContext.
         // Supported: SSL, SSLv2, SSLv3, TLS, TLSv1, TLSv1.1
         SSLContext sslContext = SSLContext.getInstance("SSL");
         sslContext.init(null, new TrustManager[]{trustManager},
                 new SecureRandom());

         // Use the above SSLContext to create your socket factory
         SSLSocketFactory sf = new SSLSocketFactory(sslContext);
         // Accept any hostname, so the self-signed certificates don't fail
         sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

         // Register our new socket factory with the typical SSL port and the
         // correct protocol name.
         Scheme httpsScheme = new Scheme("https", sf, port);
         SchemeRegistry schemeRegistry = new SchemeRegistry();
         schemeRegistry.register(httpsScheme);

         HttpParams params = new BasicHttpParams();

         ClientConnectionManager cm = new SingleClientConnManager(params,
                 schemeRegistry);

         return new DefaultHttpClient(cm, params);
      }
      catch (Exception ex)
      {

         return null;
      }
   }


   @Test
   public void testClientStressMultiple() throws Exception
   {
      for (int i = 5; i < 21; i += 5)
      {
         System.out.println();
         System.out.println();
         System.out.println("-- Test with client thread num: " + (i + 1) * 3);
         StressClient.stress(i + 1, 5, factory);
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
