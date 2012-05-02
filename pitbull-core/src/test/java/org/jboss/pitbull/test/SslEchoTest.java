package org.jboss.pitbull.test;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.jboss.pitbull.Connection;
import org.jboss.pitbull.HttpServer;
import org.jboss.pitbull.HttpServerBuilder;
import org.jboss.pitbull.RequestHeader;
import org.jboss.pitbull.StatusCode;
import org.jboss.pitbull.handlers.stream.StreamRequestHandler;
import org.jboss.pitbull.handlers.stream.StreamedResponse;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.util.ReadFromStream;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SslEchoTest
{
   public static HttpServer http;

   @BeforeClass
   public static void startup() throws Exception
   {
      http = new HttpServerBuilder()
              .connector().https().add()
              .workers(1)
              .maxRequestThreads(1).build();
      http.start();
   }

   @AfterClass
   public static void shutdown() throws Exception
   {
      http.stop();
   }


   public static class Initiator implements StreamRequestHandler
   {
      @Override
      public void execute(Connection connection, RequestHeader requestHeader, InputStream is, StreamedResponse response) throws IOException
      {
         response.setStatus(StatusCode.OK);
         response.getHeaders().addHeader("Content-Type", "text/plain");

         if (requestHeader.getMethod().equalsIgnoreCase("POST"))
         {
            byte[] bytes = ReadFromStream.readFromStream(1024, is);
            response.getOutputStream().write(bytes);
         }
         else if (requestHeader.getMethod().equalsIgnoreCase("GET"))
         {
            response.getOutputStream().write("How are you".getBytes());
         }
      }
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
   public void testEcho() throws Exception
   {
      Initiator resource = new Initiator();
      http.register("/echo{(/.*)*}", resource);

      HttpClient httpClient = createHttpClient(8443);
      ApacheHttpClient4Executor executor = new ApacheHttpClient4Executor(httpClient);

      //Thread.sleep(100000000);
      //http.getRegistry().add("/echo/{.*}", resource);

      try
      {
         ClientRequest request = executor.createRequest("https://localhost:8443/echo");
         ClientResponse res = request.body("text/plain", "hello world").post();
         Assert.assertEquals(200, res.getStatus());
         Assert.assertEquals("hello world", res.getEntity(String.class));
      }
      finally
      {
         http.unregister(resource);

      }

   }

}
