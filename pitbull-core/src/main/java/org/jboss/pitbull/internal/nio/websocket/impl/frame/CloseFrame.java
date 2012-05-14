package org.jboss.pitbull.internal.nio.websocket.impl.frame;

import org.jboss.pitbull.internal.nio.websocket.impl.FrameType;

/**
 * @author Mike Brock
 */
public class CloseFrame extends AbstractFrame
{
   public CloseFrame()
   {
      super(FrameType.ConnectionClose);
   }
}
