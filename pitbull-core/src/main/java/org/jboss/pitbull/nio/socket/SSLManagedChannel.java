package org.jboss.pitbull.nio.socket;

import org.jboss.pitbull.logging.Logger;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SSLManagedChannel extends ManagedChannel
{
   protected SSLEngine engine;
   protected SSLSession sslSession;
   protected ByteBuffer inputBuffer;
   protected ByteBuffer appBuffer;
   protected ByteBuffer outputBuffer;
   protected SSLEngineResult.HandshakeStatus handshakeStatus;
   protected SSLEngineResult.Status engineStatus = null;
   protected ByteBuffer dummy = ByteBuffer.allocate(0);

   protected static final Logger log = Logger.getLogger(SSLManagedChannel.class);

   protected boolean handshakeFinished;

   public SSLManagedChannel(SocketChannel channel, EventHandler handler, SSLEngine engine) throws Exception
   {
      super(channel, handler);
      this.engine = engine;
      this.sslSession = engine.getSession();
      inputBuffer = ByteBuffer.allocate(sslSession.getPacketBufferSize());
      outputBuffer = ByteBuffer.allocate(sslSession.getPacketBufferSize());
      appBuffer = ByteBuffer.allocate(sslSession.getApplicationBufferSize());
      engine.beginHandshake();
      handshakeStatus = engine.getHandshakeStatus();
   }

   @Override
   public SSLSession getSslSession()
   {
      return sslSession;
   }

   protected void executeEngineTasks()
   {
      Runnable task;
      while ((task = engine.getDelegatedTask()) != null)
      {
         task.run();
      }
      handshakeStatus = engine.getHandshakeStatus();
   }

   private int unwrapBuffer(int bytesRead) throws IOException
   {
      if (bytesRead == -1)
      {
         // We will not receive any more data. Closing the engine
         // is a signal that the end of stream was reached.
         engine.closeInbound();
         // EOF. But do we still have some useful data available?
         if (inputBuffer.position() == 0 ||
                 engineStatus == SSLEngineResult.Status.BUFFER_UNDERFLOW)
         {
            // Yup. Either the buffer is empty or it's in underflow,
            // meaning that there is not enough data to reassemble a
            // TLS packet. So we can return EOF.
            return -1;
         }
         // Although we reach EOF, we still have some data left to
         // be decrypted. We must process it
      }
      else if (bytesRead == 0)
      {
         return 0;
      }

      // Prepare the application buffer to receive decrypted data
      assert !appBuffer.hasRemaining() : "Application buffer not empty";
      appBuffer.clear();

      // Prepare the net data for reading.
      inputBuffer.flip();
      SSLEngineResult res;
      do
      {
         res = engine.unwrap(inputBuffer, appBuffer);
         log.info("Unwrapping:\n" + res);
         // During an handshake renegotiation we might need to perform
         // several unwraps to consume the handshake data.
      } while (res.getStatus() == SSLEngineResult.Status.OK &&
              res.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_UNWRAP &&
              res.bytesProduced() == 0);

      // If no data was produced, and the status is still ok, try to read once more
      if (appBuffer.position() == 0 &&
              res.getStatus() == SSLEngineResult.Status.OK &&
              inputBuffer.hasRemaining())
      {
         res = engine.unwrap(inputBuffer, appBuffer);
         log.info("Unwrapping:\n" + res);
      }

      /*
         * The status may be:
         * OK - Normal operation
         * OVERFLOW - Should never happen since the application buffer is
         * 	sized to hold the maximum packet size.
         * UNDERFLOW - Need to read more data from the socket. It's normal.
         * CLOSED - The other peer closed the socket. Also normal.
         */
      engineStatus = res.getStatus();
      handshakeStatus = res.getHandshakeStatus();
      // Should never happen, the peerAppData must always have enough space
      // for an unwrap operation
      assert engineStatus != SSLEngineResult.Status.BUFFER_OVERFLOW :
              "Buffer should not overflow: " + res.toString();

      // The handshake status here can be different than NOT_HANDSHAKING
      // if the other peer closed the connection. So only check for it
      // after testing for closure.
      if (engineStatus == SSLEngineResult.Status.CLOSED)
      {
         log.debug("Connection is being closed by peer.");
         return -1;
      }

      // Prepare the buffer to be written again.
      inputBuffer.compact();
      // And the app buffer to be read.
      appBuffer.flip();

      return appBuffer.remaining();
   }

   /**
    * Process everything but reads
    *
    * @throws IOException
    */
   protected void processHandshake() throws IOException
   {
      SSLEngineResult res;
      for (; ; )
      {
         handshakeStatus = engine.getHandshakeStatus();
         switch (handshakeStatus)
         {
            case FINISHED:
               return;
            case NEED_TASK:
               executeEngineTasks();
               break;
            case NEED_UNWRAP:
               // let reading handle this
               return;
            case NEED_WRAP:
               outputBuffer.clear();
               res = engine.wrap(dummy, outputBuffer);
               handshakeStatus = res.getHandshakeStatus();
               outputBuffer.flip();
               super.writeBlocking(outputBuffer);
               break;
            case NOT_HANDSHAKING:
               return;
         }
      }


   }

   private int readBuffer(ByteBuffer buf)
   {
      if (appBuffer.hasRemaining())
      {
         int limit = Math.min(appBuffer.remaining(), buf.remaining());
         for (int i = 0; i < limit; i++) {
            buf.put(appBuffer.get());
         }
         return limit;
      }
      return 0;
   }

   // NOTE: a lot of duplicate code in read methods.  Tried to use a closure-like construct,
   // but there's no way to call a super method of parent from inner class that I know of.

   @Override
   public int read(ByteBuffer buf) throws IOException
   {
      int bufBytesRead = readBuffer(buf);
      if (bufBytesRead > 0) return bufBytesRead;

      // do everything but reading from channel
      processHandshake();

      int bytesRead = super.read(inputBuffer);
      // let unwrapBuffer handle -1 and 0 values
      bytesRead = unwrapBuffer(bytesRead);
      if (bytesRead < 1) return bytesRead;

      return readBuffer(buf);
   }

   @Override
   public int readBlocking(ByteBuffer buf) throws IOException
   {
      int bufBytesRead = readBuffer(buf);
      if (bufBytesRead > 0) return bufBytesRead;

      // do everything but reading from channel
      processHandshake();

      int bytesRead = super.readBlocking(inputBuffer);
      // let unwrapBuffer handle -1 and 0 values
      bytesRead = unwrapBuffer(bytesRead);
      if (bytesRead < 1) return bytesRead;

      return readBuffer(buf);
   }

   @Override
   public int readBlocking(ByteBuffer buf, long time, TimeUnit unit) throws IOException
   {
      int bufBytesRead = readBuffer(buf);
      if (bufBytesRead > 0) return bufBytesRead;

      // do everything but reading from channel
      processHandshake();

      int bytesRead = super.readBlocking(inputBuffer, time, unit);
      // let unwrapBuffer handle -1 and 0 values
      bytesRead = unwrapBuffer(bytesRead);
      if (bytesRead < 1) return bytesRead;

      return readBuffer(buf);
   }

   @Override
   public int write(ByteBuffer buf) throws IOException
   {
      return writeBlocking(buf);
   }

   @Override
   public int writeBlocking(ByteBuffer buffer) throws IOException
   {
      outputBuffer.clear();
      SSLEngineResult res = engine.wrap(buffer, outputBuffer);
      // Prepare the buffer for reading
      outputBuffer.flip();
      super.writeBlocking(outputBuffer);

      // Return the number of bytes read
      // from the source buffer
      return res.bytesConsumed();
   }

   @Override
   public int writeBlocking(ByteBuffer buffer, long time, TimeUnit unit) throws IOException
   {
      outputBuffer.clear();
      SSLEngineResult res = engine.wrap(buffer, outputBuffer);
      // Prepare the buffer for reading
      outputBuffer.flip();
      super.writeBlocking(outputBuffer, time, unit);

      // Return the number of bytes read
      // from the source buffer
      return res.bytesConsumed();
   }

}
