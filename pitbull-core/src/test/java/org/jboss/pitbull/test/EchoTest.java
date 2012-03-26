package org.jboss.pitbull.test;

import org.jboss.pitbull.nio.http.PitbullServer;
import org.jboss.pitbull.nio.http.PitbullServerBuilder;
import org.junit.BeforeClass;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class EchoTest
{
   public static PitbullServer server;

   @BeforeClass
   public static void startup() throws Exception
   {
      /*
      server = new PitbullServerBuilder().build();
      server.setNumWorkers(1);
      server.setNumExecutors(1);
      server.start();
      */
   }

}
