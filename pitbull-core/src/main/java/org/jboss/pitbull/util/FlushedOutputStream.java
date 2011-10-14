package org.jboss.pitbull.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Works the same as BufferedOutputStream except it invokes a callback prior to:
 * - initial flush of buffer
 * - subsequent flush of buffer
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class FlushedOutputStream extends OutputStream
{
   protected OutputStream delegate;

   protected byte buf[];
   protected int numWritten;
   protected boolean initialFlush = true;
   protected int size;

   /**
    * delegate OutputStream can be null and set at another time (i.e. at initialFlush time)
    *
    * @param out
    */
   public FlushedOutputStream(OutputStream out)
   {
      this(out, 8192);
   }

   /**
    * delegate OutputStream can be null and set at another time (i.e. at initialFlush time)
    *
    * @param out
    * @param size must be > 0
    */
   public FlushedOutputStream(OutputStream out, int size)
   {
      this.delegate = out;
      setBufferSize(size);
   }

   private void flushBuffer() throws IOException
   {
      if (numWritten > 0)
      {
         if (initialFlush)
         {
            initialFlush = false;
            initialFlush();
         }
         else
         {
            anotherFlush();
         }
         delegate.write(buf, 0, numWritten);
         numWritten = 0;
      }
   }

   protected void initialFlush() throws IOException
   {
      // complete
   }
   protected void anotherFlush() throws IOException
   {
      // complete
   }

   public void reset() throws IllegalStateException
   {
      if (initialFlush == false) throw new IllegalStateException("Buffer was flushed");
      numWritten = 0;
   }

   public int getBufferSize()
   {
      return size;
   }

   public void setBufferSize(int size) throws IllegalStateException
   {
      if (size <= 0)
      {
         throw new IllegalArgumentException("Cannot set a buffer size that is less than zero.");
      }
      if (numWritten > 0) throw new IllegalStateException("Buffer has been written to, cannot reset size");
      this.size = size;
      buf = new byte[size];
   }


   public synchronized void write(int b) throws IOException
   {
      if (numWritten >= buf.length)
      {
         flushBuffer();
      }
      buf[numWritten++] = (byte) b;
   }


   public synchronized void write(byte b[], int off, int len) throws IOException
   {
      if (len >= buf.length)
      {
         flushBuffer();
         delegate.write(b, off, len);
         return;
      }

      if (len > buf.length - numWritten)
      {
         flushBuffer();
      }

      System.arraycopy(b, off, buf, numWritten, len);
      numWritten += len;
   }


   public synchronized void flush() throws IOException
   {
      flushBuffer();
      delegate.flush();
   }
}

