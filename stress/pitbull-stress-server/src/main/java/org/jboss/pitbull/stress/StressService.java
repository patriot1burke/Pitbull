package org.jboss.pitbull.stress;

import org.jboss.pitbull.Connection;
import org.jboss.pitbull.server.HttpServer;
import org.jboss.pitbull.server.HttpServerBuilder;
import org.jboss.pitbull.RequestHeader;
import org.jboss.pitbull.StatusCode;
import org.jboss.pitbull.server.handlers.stream.StreamRequestHandler;
import org.jboss.pitbull.server.handlers.stream.StreamedResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class StressService implements StreamRequestHandler
{
   public static byte[] readFromStream(int bufferSize, InputStream entityStream)
           throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      byte[] buffer = new byte[bufferSize];
      int wasRead = 0;
      do
      {
         wasRead = entityStream.read(buffer);
         if (wasRead > 0)
         {
            baos.write(buffer, 0, wasRead);
         }
      } while (wasRead > -1);
      return baos.toByteArray();
   }

   public static AtomicLong success = new AtomicLong(0);
   public static AtomicLong hits = new AtomicLong(0);
   public static Object lock = new Object();


   @Override
   public void execute(Connection connection, RequestHeader requestHeader, InputStream requestBody, StreamedResponse response) throws IOException
   {
      hits.incrementAndGet();
      if (requestHeader.getMethod().equalsIgnoreCase("GET")) doGet(connection, requestHeader, response);
      else if (requestHeader.getMethod().equalsIgnoreCase("PUT"))
         doPut(connection, requestHeader, requestBody, response);
      else if (requestHeader.getMethod().equalsIgnoreCase("POST"))
         doPost(connection, requestHeader, requestBody, response);
      else throw new RuntimeException("Unkown Method");
      success.incrementAndGet();
   }

   public void doGet(Connection connection, RequestHeader requestHeader, StreamedResponse response) throws IOException
   {
      response.setStatus(StatusCode.OK);
      response.getHeaders().addHeader("Content-Type", "text/plain");
      response.getOutputStream().write("DO GET".getBytes());

   }

   public void doPut(Connection connection, RequestHeader requestHeader, InputStream requestBody, StreamedResponse response) throws IOException
   {
      response.setStatus(StatusCode.NO_CONTENT);
   }

   public void doPost(Connection connection, RequestHeader requestHeader, InputStream requestBody, StreamedResponse response) throws IOException
   {
      response.setStatus(StatusCode.OK);
      response.getHeaders().addHeader("Content-Type", "text/plain");
      byte[] bytes = readFromStream(1024, requestBody);
      response.getOutputStream().write(bytes);

   }

   public static void main(String[] args) throws Exception
   {
      Runtime.getRuntime().addShutdownHook(new Thread()
      {
         public void run()
         {
            System.out.println();
            System.out.println("-- Server shutdown --");
            System.out.println("Total hits: " + hits.longValue());
            System.out.println("Total success: " + success.longValue());
         }
      });

      int workers = 4;
      int requestThreads = 100;

      if (args.length >= 1)
      {
         try
         {
            int tmp = Integer.parseInt(args[0]);
            if (tmp < 1)
            {
               System.out.println("Can't have less than 1 worker");
               return;
            }
            workers = tmp;
         }
         catch (NumberFormatException e)
         {

         }
      }

      if (args.length >= 2)
      {
         try
         {
            int tmp = Integer.parseInt(args[1]);
            if (tmp < 1)
            {
               System.out.println("Can't have less than 1 requestThread");
               return;
            }
            requestThreads = tmp;
         }
         catch (NumberFormatException e)
         {

         }
      }
      start(workers, requestThreads);

      synchronized (lock)
      { lock.wait(); }
   }

   public static HttpServer start(int workers, int requestThreads) throws Exception
   {
      HttpServer http = new HttpServerBuilder().connector().add()
              .workers(workers)
              .maxRequestThreads(requestThreads).build();
      http.start();
      http.register("/{.*}", new StressService());
      return http;
   }
}
