package org.jboss.pitbull.internal.nio.websocket.impl.frame;

import org.jboss.pitbull.internal.nio.websocket.impl.FrameType;

/**
 * @author Mike Brock
 */
public class PingFrame extends AbstractFrame
{
   public PingFrame()
   {
      super(FrameType.Ping);
   }
}
