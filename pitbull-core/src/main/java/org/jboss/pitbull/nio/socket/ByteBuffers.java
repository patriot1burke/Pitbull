package org.jboss.pitbull.nio.socket;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ByteBuffers
{
   public static void skip(ByteBuffer buffer, int cnt)
   {
      if (cnt < 0)
      {
         throw new IllegalArgumentException();
      }
      if (cnt > buffer.remaining())
      {
         throw new BufferUnderflowException();
      }
      buffer.position(buffer.position() + cnt);
   }
}
