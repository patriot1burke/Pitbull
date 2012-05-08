package org.jboss.pitbull.handlers;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper over a NIO Channel that allows you to do blocking and non-blocking reads/writes.  Also can do SSL
 * underneath without having to worry about the intricacies of SSL.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface PitbullChannel
{
   String getId();

   SSLSession getSslSession();

   SocketChannel getChannel();

   /**
    * Non-blocking
    *
    * @param buf
    * @return
    * @throws java.io.IOException
    */
   int read(ByteBuffer buf) throws IOException;

   int readBlocking(ByteBuffer buf) throws IOException;

   int readBlocking(ByteBuffer buf, long time, TimeUnit unit) throws IOException;

   /**
    * Non-blocking
    *
    * @param buf
    * @return
    * @throws java.io.IOException
    */
   int write(ByteBuffer buf) throws IOException;

   int writeBlocking(ByteBuffer buffer) throws IOException;

   int writeBlocking(ByteBuffer buffer, long time, TimeUnit unit) throws IOException;

   boolean isClosed();

   void close();
}
