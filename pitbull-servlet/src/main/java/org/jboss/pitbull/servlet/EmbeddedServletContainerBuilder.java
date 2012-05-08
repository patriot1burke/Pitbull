package org.jboss.pitbull.servlet;

import org.jboss.pitbull.server.HttpServer;
import org.jboss.pitbull.server.HttpServerBuilder;

public class EmbeddedServletContainerBuilder extends HttpServerBuilder<EmbeddedServletContainerBuilder, EmbeddedServletContainer>
{
   @Override
   protected HttpServer create()
   {
      return new EmbeddedServletContainer();
   }
}