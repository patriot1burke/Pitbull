package org.jboss.pitbull.stress;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class StressClient
{
   public static AtomicLong attempts = new AtomicLong();
   public static AtomicLong success = new AtomicLong();
   public static volatile boolean shutdown = false;

   public interface ExecutorFactory
   {
      String getUrl();

      ApacheHttpClient4Executor create();
   }


   public static class PutRunnable implements Runnable
   {
      protected ExecutorFactory factory;

      public PutRunnable(ExecutorFactory factory)
      {
         this.factory = factory;
      }

      @Override
      public void run()
      {
         ApacheHttpClient4Executor executor = factory.create();

         try
         {
            while (true)
            {
               synchronized (this)
               {
                  boolean s = shutdown;
                  if (s) return;
               }
               attempts.incrementAndGet();
               // "http://localhost:8080/echo"
               ClientRequest request = executor.createRequest(factory.getUrl());
               try
               {
                  ClientResponse res = request.body("text/plain", "hello world").put();
                  try
                  {
                     if (res.getStatus() == 204)
                     {
                        success.incrementAndGet();
                     }
                  }
                  finally
                  {
                     res.releaseConnection();
                  }
               }
               catch (Exception e)
               {

               }
            }
         }
         finally
         {
            executor.close();
         }
      }
   }


   public static class PostRunnable implements Runnable
   {
      protected ExecutorFactory factory;

      public PostRunnable(ExecutorFactory factory)
      {
         this.factory = factory;
      }

      @Override
      public void run()
      {
         ApacheHttpClient4Executor executor = factory.create();

         try
         {
            while (true)
            {
               synchronized (this)
               {
                  boolean s = shutdown;
                  if (s) return;
               }
               attempts.incrementAndGet();
               // "http://localhost:8080/echo"
               ClientRequest request = executor.createRequest(factory.getUrl());
               try
               {
                  ClientResponse res = request.body("text/plain", "hello world").post();
                  try
                  {
                     if (res.getStatus() == 200 && "hello world".equals(res.getEntity(String.class)))
                     {
                        success.incrementAndGet();
                     }
                  }
                  finally
                  {
                     res.releaseConnection();
                  }
               }
               catch (Exception e)
               {

               }
            }
         }
         finally
         {
            executor.close();
         }
      }
   }

   public static class GetRunnable implements Runnable
   {
      protected ExecutorFactory factory;

      public GetRunnable(ExecutorFactory factory)
      {
         this.factory = factory;
      }

      @Override
      public void run()
      {
         ApacheHttpClient4Executor executor = factory.create();

         try
         {
            while (true)
            {
               synchronized (this)
               {
                  boolean s = shutdown;
                  if (s) return;
               }
               attempts.incrementAndGet();
               // "http://localhost:8080/echo"
               ClientRequest request = executor.createRequest(factory.getUrl());
               try
               {
                  ClientResponse res = request.get();
                  try
                  {
                     if (res.getStatus() == 200 && "DO GET".equals(res.getEntity(String.class)))
                     {
                        success.incrementAndGet();
                     }
                  }
                  finally
                  {
                     res.releaseConnection();
                  }
               }
               catch (Exception e)
               {

               }
            }
         }
         finally
         {
            executor.close();
         }
      }
   }

   public static void stress(int jobMultiple, long secs) throws Exception
   {
      stress(jobMultiple, secs, new ExecutorFactory()
      {
         @Override
         public String getUrl()
         {
            return "http://localhost:8080/echo";
         }

         @Override
         public ApacheHttpClient4Executor create()
         {
            return new ApacheHttpClient4Executor();
         }
      });
   }


   public static void stress(int jobMultiple, long secs, ExecutorFactory factory) throws Exception
   {
      attempts.set(0);
      success.set(0);
      shutdown = false;
      if (jobMultiple < 1) jobMultiple = 1;
      Thread[] threads = new Thread[jobMultiple * 3];

      for (int i = 0; i < jobMultiple * 3; i++)
      {
         threads[i++] = new Thread(new PutRunnable(factory));
         threads[i++] = new Thread(new GetRunnable(factory));
         threads[i] = new Thread(new PostRunnable(factory));
      }

      long start = System.currentTimeMillis();
      for (int i = 0; i < jobMultiple * 3; i++)
      {
         threads[i].start();
      }

      Thread.sleep(secs * 1000);

      synchronized (threads)
      {
         shutdown = true;
      }
      for (int i = 0; i < jobMultiple * 3; i++)
      {
         threads[i].join();
      }
      long end = System.currentTimeMillis() - start;
      System.out.println("Test ran in : " + end + " (ms)");
      System.out.println("Attempts: " + attempts.longValue());
      System.out.println("Success: " + success.longValue());
      System.out.println("Throughput: " + ((double) attempts.longValue()) / (((double) end)));
   }

   public static void main(String[] args) throws Exception
   {
      int jobMultiple = 1;
      int secs = 5;

      if (args.length >= 1)
      {
         try
         {
            int tmp = Integer.parseInt(args[0]);
            if (tmp < 1)
            {
               System.out.println("Can't have less than 1 jobMultiple");
               return;
            }
            jobMultiple = tmp;
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
               System.out.println("Can't have less than 1 secs");
               return;
            }
            secs = tmp;
         }
         catch (NumberFormatException e)
         {

         }
      }
      stress(jobMultiple, secs);
   }
}
