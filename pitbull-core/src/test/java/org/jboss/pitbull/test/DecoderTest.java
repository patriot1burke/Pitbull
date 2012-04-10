package org.jboss.pitbull.test;

import org.jboss.pitbull.internal.nio.http.HttpRequestDecoder;
import org.jboss.pitbull.internal.nio.http.HttpRequestHeader;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class DecoderTest
{

   static byte[] bytes(String s) throws Exception
   {
      return s.getBytes("UTF-8");
   }

   @Test
   public void testDecoder() throws Exception
   {
      HttpRequestDecoder decoder = new HttpRequestDecoder();

      ByteBuffer buf = ByteBuffer.allocate(1000);

      buf.put(bytes("GET /foo/bar HTTP/1.1\r\n"));
      buf.put(bytes("Host: localhost\r\n"));
      buf.put(bytes("Date: today\r\n"));
      buf.put(bytes("\r\n"));
      buf.flip();

      Assert.assertTrue(decoder.process(buf));

      HttpRequestHeader header = decoder.getRequest();
      Assert.assertEquals(header.getMethod(), "GET");
      Assert.assertEquals(header.getUri(), "/foo/bar");
      Assert.assertEquals(2, header.getHeaders().size());
      Assert.assertEquals("localhost", header.getHeaders().getFirst("Host"));

   }

   public static class SlowProcessor
   {
      protected int index;
      protected byte[] bytes;
      protected HttpRequestDecoder decoder;
      protected ByteBuffer buf = ByteBuffer.allocate(1000);

      public SlowProcessor(byte[] bytes, HttpRequestDecoder decoder)
      {
         this.bytes = bytes;
         this.decoder = decoder;
      }

      public boolean process(int num)
      {
         int len = num;
         if (index + num > bytes.length)
         {
            len = bytes.length - index;
            if (len < 1) return false;
         }
         buf.clear();
         try
         {
            buf.put(bytes, index, len);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
         index += len;
         buf.flip();
         decoder.process(buf);
         return index < bytes.length;
      }


   }

   @Test
   public void testIncompleteBuffer() throws Exception
   {


      String req = "GET /foo/bar HTTP/1.1\r\nHost: localhost\r\n\r\n";
      byte[] bytes = bytes(req);

      // test all buffer lengths
      for (int i = 1; i <= bytes.length; i++)
      {

         HttpRequestDecoder decoder = new HttpRequestDecoder();
         SlowProcessor processor = new SlowProcessor(bytes, decoder);

         while (processor.process(i)) ;

         HttpRequestHeader header = decoder.getRequest();
         Assert.assertEquals(header.getMethod(), "GET");
         Assert.assertEquals(header.getUri(), "/foo/bar");
         Assert.assertEquals(1, header.getHeaders().size());
         Assert.assertEquals("localhost", header.getHeaders().getFirst("Host"));
      }


   }

   @Test
   public void testIncompleteBuffer2() throws Exception
   {


      String req = "   GET /foo/bar HTTP/1.1\r\nHost: localhost\r\nDate: foo\r\nAccept: text/html;\r\n\t application/xml;\r\n text/plain\r\n\r\n";
      byte[] bytes = bytes(req);

      // test all buffer lengths
      for (int i = 1; i <= bytes.length; i++)
      {

         HttpRequestDecoder decoder = new HttpRequestDecoder();
         SlowProcessor processor = new SlowProcessor(bytes, decoder);

         while (processor.process(i)) ;

         HttpRequestHeader header = decoder.getRequest();
         Assert.assertEquals(header.getMethod(), "GET");
         Assert.assertEquals(header.getUri(), "/foo/bar");
         Assert.assertEquals(3, header.getHeaders().size());
         Assert.assertEquals("localhost", header.getHeaders().getFirst("Host"));
         Assert.assertEquals("text/html; application/xml; text/plain", header.getHeaders().getFirst("Accept"));
      }


   }

}
