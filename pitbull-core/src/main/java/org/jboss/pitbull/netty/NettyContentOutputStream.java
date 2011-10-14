package org.jboss.pitbull.netty;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.pitbull.spi.ContentOutputStream;

import java.io.IOException;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class NettyContentOutputStream extends ContentOutputStream
{
   public abstract ChannelFuture getLastFuture();
}
