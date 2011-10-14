package org.jboss.pitbull.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface HandlerStrategy
{
   void beginChunked();

   void addChunk(ChannelBuffer chunk);

   public void lastChunk(ChannelBuffer chunk, List<Map.Entry<String, String>> trailHeaders, NettyRequestHeader requestHeader, Channel channel);

   public void notChunked(NettyRequestHeader requestHeader, Channel channel);

   void disconnected();
}
