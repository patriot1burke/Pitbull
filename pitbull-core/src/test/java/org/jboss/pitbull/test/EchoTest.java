package org.jboss.pitbull.test;

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
import org.jboss.resteasy.util.ReadFromStream;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class EchoTest
{
   public static HttpServer http;

   @BeforeClass
   public static void startup() throws Exception
   {
      http = new HttpServerBuilder().connector().add()
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
                  if (requestHeader.getMethod().equalsIgnoreCase("POST"))
                  {
                     byte[] bytes = ReadFromStream.readFromStream(1024, is);
                     os.write(bytes);
                  }
                  else if (requestHeader.getMethod().equalsIgnoreCase("GET"))
                  {
                     os.write("How are you".getBytes());
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

   @Test
   public void test404() throws Exception
   {
      ClientRequest request = new ClientRequest("http://localhost:8080/notfound");
      ClientResponse res = request.get();
      Assert.assertEquals(404, res.getStatus());
   }

   @Test
   public void testEcho() throws Exception
   {
      Initiator resource = new Initiator();
      http.getRegistry().add("/echo{(/.*)*}", resource);

      //Thread.sleep(100000000);

      //http.getRegistry().add("/echo/{.*}", resource);

      try
      {
         ClientRequest request = new ClientRequest("http://localhost:8080/echo");
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
