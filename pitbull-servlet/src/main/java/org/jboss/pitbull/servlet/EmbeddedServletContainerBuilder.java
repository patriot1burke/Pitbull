package org.jboss.pitbull.servlet;

import org.jboss.pitbull.HttpServer;
import org.jboss.pitbull.HttpServerBuilder;

public class EmbeddedServletContainerBuilder extends HttpServerBuilder<EmbeddedServletContainerBuilder, EmbeddedServletContainer>
{
   @Override
   protected HttpServer create()
   {
      return new EmbeddedServletContainer();
   }
}