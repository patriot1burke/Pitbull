package org.jboss.pitbull.handlers.websocket;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface TextFrame extends Frame
{
   /**
    * Will block until the entire text frame is read.
    *
    * @return
    */
   String getText();

   /**
    * Get character encoding of text frame.  Usually UTF-8.
    *
    * @return
    */
   String getEncoding();
}
