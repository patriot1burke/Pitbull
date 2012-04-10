package org.jboss.pitbull.servlet.internal;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ServletInputStreamImpl extends ServletInputStream
{
   protected InputStream delegate;

   public ServletInputStreamImpl(InputStream delegate)
   {
      this.delegate = delegate;
   }

   @Override
   public int read() throws IOException
   {
      return delegate.read();
   }

   @Override
   public int read(byte[] bytes) throws IOException
   {
      return delegate.read(bytes);
   }

   @Override
   public int read(byte[] bytes, int i, int i1) throws IOException
   {
      return delegate.read(bytes, i, i1);
   }

   @Override
   public long skip(long l) throws IOException
   {
      return delegate.skip(l);
   }

   @Override
   public int available() throws IOException
   {
      return delegate.available();
   }

   @Override
   public void close() throws IOException
   {
      delegate.close();
   }

   @Override
   public void mark(int i)
   {
      delegate.mark(i);
   }

   @Override
   public void reset() throws IOException
   {
      delegate.reset();
   }

   @Override
   public boolean markSupported()
   {
      return delegate.markSupported();
   }
}
