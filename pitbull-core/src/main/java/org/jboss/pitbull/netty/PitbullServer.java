package org.jboss.pitbull.netty;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.pitbull.util.registry.UriRegistry;
import org.jboss.pitbull.spi.RequestInitiator;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class PitbullServer
{
   protected ServerBootstrap bootstrap;
   protected Channel channel;
   protected int port = 8080;
   protected String root = "";
   protected UriRegistry<RequestInitiator> registry = new UriRegistry<RequestInitiator>();

   public int getPort()
   {
      return port;
   }

   public void setPort(int port)
   {
      this.port = port;
   }

   public UriRegistry<RequestInitiator> getRegistry()
   {
      return registry;
   }

   public void start()
   {
      // Configure the server.
      ServerBootstrap bootstrap = new ServerBootstrap(
              new NioServerSocketChannelFactory(
                      Executors.newCachedThreadPool(),
                      Executors.newCachedThreadPool()));

      // Set up the event pipeline factory.
      bootstrap.setPipelineFactory(new NettyPipelineFactory(registry));

      // Bind and start to accept incoming connections.
      channel = bootstrap.bind(new InetSocketAddress(port));
   }

   public void stop()
   {
      ChannelFuture future = channel.close();
      try
      {
         future.await(5000);
      }
      catch (InterruptedException e)
      {

      }
   }
}