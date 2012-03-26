package org.jboss.pitbull.servlet;

import org.jboss.pitbull.crypto.KeyTools;
import org.jboss.pitbull.nio.http.PitbullServer;
import org.jboss.pitbull.nio.http.PitbullServerBuilder;

import java.security.KeyStore;

public class EmbeddedServletContainerBuilder extends PitbullServerBuilder<EmbeddedServletContainerBuilder, EmbeddedServletContainer>
{
   @Override
   protected PitbullServer create()
   {
      return new EmbeddedServletContainer();
   }
}