package org.jboss.pitbull.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class Util
{
   public static String readString(InputStream in, String charset) throws IOException
   {
      byte[] buffer = new byte[1024];
      ByteArrayOutputStream builder = new ByteArrayOutputStream();
      int wasRead = 0;
      StringBuilder sb = new StringBuilder();
      do
      {
         wasRead = in.read(buffer, 0, 1024);
         if (wasRead > 0)
         {
            builder.write(buffer, 0, wasRead);
            for (int i = 0; i < wasRead; i++) sb.append((char) buffer[i]);
            if (true)
            { int x = 1; }  // this just here so i can set breakpoint when debugging
         }
      }
      while (wasRead > -1);
      byte[] bytes = builder.toByteArray();

      if (charset != null) return new String(bytes, charset);
      else return new String(bytes, "UTF-8");
   }
}
