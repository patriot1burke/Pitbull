package org.jboss.pitbull.internal.nio.websocket.impl.frame;

import org.jboss.pitbull.internal.nio.websocket.impl.FrameType;

/**
 * @author Mike Brock
 */
public class PongFrame extends AbstractFrame
{
   public PongFrame()
   {
      super(FrameType.Pong);
   }
}
