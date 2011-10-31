package org.jboss.pitbull.nio;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface EventHandler
{
   void handleRead(ManagedChannel channel);
   void shutdown();
}
