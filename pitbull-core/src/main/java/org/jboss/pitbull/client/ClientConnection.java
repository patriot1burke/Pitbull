package org.jboss.pitbull.client;

import org.jboss.pitbull.Connection;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ClientConnection extends Connection
{
   public String getHost();
   public int getPort();

   /**
    * @param uri relative URI minus host/port/protocol
    * @return
    */
   public ClientInvocation request(String uri);

   public boolean isClosed();
   public void close();
}
