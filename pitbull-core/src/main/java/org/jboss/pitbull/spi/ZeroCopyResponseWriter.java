package org.jboss.pitbull.spi;

import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ZeroCopyResponseWriter
{
   void write(ResponseHeader responseHeader, FileChannel fileChannel);
}
