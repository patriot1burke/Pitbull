package org.jboss.pitbull.spi;

import java.util.List;
import java.util.Map;

/**
 * This is just an example of what we might do for chunked input.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Deprecated
interface ChunkHandler
{
   void addChunk(byte[] bytes);
   void endChunk(List<Map.Entry<String, String>> trailerHeaders, byte[] bytes);
}
