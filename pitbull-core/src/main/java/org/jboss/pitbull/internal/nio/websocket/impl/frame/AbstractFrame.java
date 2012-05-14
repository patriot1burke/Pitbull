package org.jboss.pitbull.internal.nio.websocket.impl.frame;

import org.jboss.pitbull.internal.nio.websocket.impl.Frame;
import org.jboss.pitbull.internal.nio.websocket.impl.FrameType;

/**
 * @author Mike Brock
 */
public class AbstractFrame implements Frame
{
   private final FrameType type;

   protected AbstractFrame(final FrameType type)
   {
      this.type = type;
   }

   public FrameType getType()
   {
      return type;
   }
}
