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
import org.jboss.pitbull.HttpServer;
import org.jboss.pitbull.HttpServerBuilder;
import org.jboss.pitbull.spi.Connection;
import org.jboss.pitbull.spi.ContentOutputStream;
import org.jboss.pitbull.spi.OrderedHeaders;
import org.jboss.pitbull.spi.RequestHandler;
import org.jboss.pitbull.spi.RequestHeader;
import org.jboss.pitbull.spi.RequestInitiator;
import org.jboss.pitbull.spi.ResponseHeader;
import org.jboss.pitbull.spi.StreamHandler;
import org.jboss.pitbull.spi.StreamResponseWriter;
import org.jboss.pitbull.util.OrderedHeadersImpl;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

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


   public static class Initiator implements RequestInitiator
   {
      @Override
      public RequestHandler begin(Connection connection, RequestHeader requestHeader)
      {
         return new StreamHandler()
         {
            protected InputStream is;
            protected StreamResponseWriter writer;

            @Override
            public void setInputStream(InputStream input)
            {
               is = input;
            }

            @Override
            public void setWriter(StreamResponseWriter writer)
            {
               this.writer = writer;
            }

            @Override
            public boolean canExecuteInWorkerThread()
            {
               return false;
            }

            @Override
            public void execute(RequestHeader requestHeader)
            {
               ResponseHeader res = new ResponseHeader()
               {
                  @Override
                  public int getStatus()
                  {
                     return 200;
                  }

                  @Override
                  public String getStatusMessage()
                  {
                     return "OK";
                  }

                  @Override
                  public OrderedHeaders getHeaders()
                  {
                     OrderedHeaders headers = new OrderedHeadersImpl();
                     headers.addHeader("Content-Type", "text/plain");
                     return headers;
                  }
               };

               ContentOutputStream os = writer.getStream(res);
               try
               {
                  System.out.println("**** HERE ***");
                  if (requestHeader.getMethod().equalsIgnoreCase("POST"))
                  {
                     byte[] bytes = ReadFromStream.readFromStream(1024, is);
                     os.write(bytes);
                  }
                  else if (requestHeader.getMethod().equalsIgnoreCase("GET"))
                  {
                     String msg = "SSL: How Are You: " + new Date();
                     os.write(msg.getBytes());
                  }
               }
               catch (IOException e)
               {
                  throw new RuntimeException(e);
               }
               writer.end(res);
            }

            @Override
            public void unsupportedHandler()
            {
            }
         };
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
      http.getRegistry().add("/echo{(/.*)*}", resource);

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
         http.getRegistry().remove(resource);

      }

   }

}
