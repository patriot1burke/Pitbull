package org.jboss.pitbull.spi;

import java.io.OutputStream;

/**
 * Used to lazily write content back to client.  Can be reset and buffer size set as well.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class ContentOutputStream extends OutputStream
{
   public abstract void reset() throws IllegalStateException;

   public abstract int getBufferSize();

   public abstract void setBufferSize(int size);

   public abstract boolean isCommitted();
}

