package org.jboss.pitbull.internal.nio.http;

import java.io.IOException;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class NullInputStream extends ContentInputStream
{
   @Override
   public void eat() throws IOException
   {
   }

   @Override
   public int read() throws IOException
   {
      return -1;
   }

   @Override
   public int read(byte[] bytes) throws IOException
   {
      return -1;
   }

   @Override
   public int read(byte[] bytes, int i, int i1) throws IOException
   {
      return -1;
   }

   @Override
   public long skip(long l) throws IOException
   {
      return 0L;
   }

   @Override
   public int available() throws IOException
   {
      return 0;
   }
}
