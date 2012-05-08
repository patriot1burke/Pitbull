package org.jboss.pitbull.websocket;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface BinaryFrame extends Frame
{
   /**
    * This will block until the entire binary frame is read.
    *
    * @return
    */
   byte[] getBytes();
}
