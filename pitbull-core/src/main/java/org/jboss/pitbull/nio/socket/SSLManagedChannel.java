package org.jboss.pitbull.nio.socket;

import org.jboss.pitbull.logging.Logger;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLSession;
import javax.xml.transform.OutputKeys;
import java.io.IOException;
import java.nio.ByteBuffer;
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

   public SSLManagedChannel(SocketChannel channel, EventHandler handler, SSLEngine engine) throws Exception
   {
      super(channel, handler);
      this.engine = engine;
      this.sslSession = engine.getSession();
      int packetBufferSize = sslSession.getPacketBufferSize();
      inputBuffer = ByteBuffer.allocate(packetBufferSize);
      outputBuffer = ByteBuffer.allocate(packetBufferSize);
      int applicationBufferSize = sslSession.getApplicationBufferSize();
      appBuffer = ByteBuffer.allocate(applicationBufferSize);

      // Change the position of the buffers so that a
      // call to hasRemaining() returns false. A buffer is considered
      // empty when the position is set to its limit, that is when
      // hasRemaining() returns false.
      appBuffer.position(appBuffer.limit());
      outputBuffer.position(outputBuffer.limit());

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

   /**
    * @return true if status == CLOSED
    * @throws IOException
    */
   protected boolean needUnwrap() throws IOException
   {
      if (inputBuffer.position() == 0) return false;

      log.trace("needUnwrap()");

      SSLEngineResult res;
      inputBuffer.flip();

      do
      {
         res = engine.unwrap(inputBuffer, appBuffer);
         log.trace("Unwrapping:\n" + res);
         // During an handshake renegotiation we might need to perform
         // several unwraps to consume the handshake data.
      } while (res.getStatus() == SSLEngineResult.Status.OK &&
              res.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_UNWRAP);

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
         return true;
      }

      inputBuffer.compact();
      return false;


   }

   protected int processHandshake() throws IOException
   {
      log.trace("processHandshake()");
      SSLEngineResult res;
      try
      {
         for (; ; )
         {
            handshakeStatus = engine.getHandshakeStatus();
            switch (handshakeStatus)
            {
               case FINISHED:
                  log.trace("Handshake FINISHED");
                  return appBuffer.remaining();
               case NEED_TASK:
                  log.trace("Handshake NEED_TASK");
                  executeEngineTasks();
                  break;
               case NEED_UNWRAP:
                  log.trace("Handshake NEED_UNWRAP");
                  if (inputBuffer.position() > 0
                          && engineStatus != SSLEngineResult.Status.BUFFER_UNDERFLOW)
                  {
                     if (needUnwrap())
                     {
                        return -1;
                     }
                  }
                  else
                  {
                     return appBuffer.remaining();
                  }
               case NEED_WRAP:
                  log.trace("Handshake NEED_WRAP");
                  outputBuffer.clear();
                  res = engine.wrap(dummy, outputBuffer);
                  handshakeStatus = res.getHandshakeStatus();
                  outputBuffer.flip();
                  super.writeBlocking(outputBuffer);
                  break;
               case NOT_HANDSHAKING:
                  log.trace("Handshake NOT_HANDSHAKING");
                  return appBuffer.remaining();
            }
         }
      }
      finally
      {
         handshakeStatus = engine.getHandshakeStatus();
         log.trace("End processHandshake() : {0}", handshakeStatus);
      }
   }

   private int readBuffer(ByteBuffer buf)
   {
      if (appBuffer.hasRemaining())
      {
         int limit = Math.min(appBuffer.remaining(), buf.remaining());
         for (int i = 0; i < limit; i++)
         {
            buf.put(appBuffer.get());
         }
         return limit;
      }
      return 0;
   }

   protected int readSuper(ByteBuffer buf) throws IOException
   {
      return super.read(buf);
   }

   protected int readBlockingSuper(ByteBuffer buf) throws IOException
   {
      return super.readBlocking(buf);
   }

   protected int readBlockingSuper(ByteBuffer buf, long time, TimeUnit unit) throws IOException
   {
      return super.readBlocking(buf, time, unit);
   }

   protected interface ReadExecution
   {
      int read(ByteBuffer buf) throws IOException;
   }

   protected int readExecution(ByteBuffer buf, ReadExecution execution) throws IOException
   {
      try
      {
         int bufBytesRead = readBuffer(buf);
         log.trace("Bytes read from buffer: {0}", bufBytesRead);

         if (bufBytesRead > 0)
         {
            return bufBytesRead;
         }


         // nothing in appBuffer so clear it
         appBuffer.clear();

         // do everything but reading from channel
         int bytesRead = processHandshake();
         if (bytesRead == -1)
         {
            log.trace("processEngine resulted in closed channel");
            return -1;
         }

         bytesRead = execution.read(inputBuffer);
         log.trace("Bytes read from channel: {0}", bytesRead);
         if (bytesRead < 1) return bytesRead;


         // Now that we have bytes in the buffer, do something with it.

         log.trace("Start loop--");
         do
         {
            int status = processHandshake();
            if (status == -1)
            {
               log.trace("channel closed after processHandshake");
               return -1;
            }
            log.trace("Unwrapping");
            inputBuffer.flip();
            SSLEngineResult res = engine.unwrap(inputBuffer, appBuffer);
            log.trace("Unwrapped: {0}", res);
            handshakeStatus = res.getHandshakeStatus();
            engineStatus = res.getStatus();
            inputBuffer.compact();
         } while (engineStatus == SSLEngineResult.Status.OK && inputBuffer.hasRemaining());

         log.trace("--After loop:");
         log.trace("HandshakeStatus: {0}", handshakeStatus);
         log.trace("Engine Status: {0}", engineStatus);
         // handle any need-task, need-wrap
         processHandshake();

         // Prepare the buffer to be written again.
         log.trace("prepare buffers");
         appBuffer.flip();
         log.trace("remaining inputBuffer: {0}", inputBuffer.position());

         return readBuffer(buf);
      }
      finally
      {
         log.trace("---> exit - read");
      }

   }


   public int read(ByteBuffer buf) throws IOException
   {
      log.trace("read()");
      return readExecution(buf,
              new ReadExecution()
              {
                 @Override
                 public int read(ByteBuffer buf) throws IOException
                 {
                    return readSuper(buf);
                 }
              });
   }

   @Override
   public int readBlocking(ByteBuffer buf) throws IOException
   {
      log.trace("readBlocking()");
      return readExecution(buf,
              new ReadExecution()
              {
                 @Override
                 public int read(ByteBuffer buf) throws IOException
                 {
                    return readBlockingSuper(buf);
                 }
              });
   }

   @Override
   public int readBlocking(final ByteBuffer buf, final long time, final TimeUnit unit) throws IOException
   {
      log.trace("readBlocking() with timeout");
      return readExecution(buf,
              new ReadExecution()
              {
                 @Override
                 public int read(ByteBuffer buf) throws IOException
                 {
                    return readBlockingSuper(buf, time, unit);
                 }
              });
   }

   @Override
   public int write(ByteBuffer buf) throws IOException
   {
      return writeBlocking(buf);
   }

   @Override
   public int writeBlocking(ByteBuffer buffer) throws IOException
   {
      int size = buffer.remaining();
      while (buffer.hasRemaining())
      {
         outputBuffer.clear();
         SSLEngineResult res = engine.wrap(buffer, outputBuffer);
         if (res.getStatus() != SSLEngineResult.Status.OK)
         {
            throw new IOException("Illegal status for write: " + res.getStatus());
         }
         // Prepare the buffer for reading
         outputBuffer.flip();
         int result = super.writeBlocking(outputBuffer);
         if (result == -1) return -1;
      }

      // Return the number of bytes read
      // from the source buffer
      return size;
   }

   @Override
   public int writeBlocking(ByteBuffer buffer, long time, TimeUnit unit) throws IOException
   {
      long timeRemaining = unit.toMillis(time);
      long now = System.currentTimeMillis();
      int numBufWritten = 0;
      while (buffer.hasRemaining() && timeRemaining > 0L)
      {
         outputBuffer.clear();
         SSLEngineResult res = engine.wrap(buffer, outputBuffer);
         if (res.getStatus() != SSLEngineResult.Status.OK)
         {
            throw new IOException("Illegal status for write: " + res.getStatus());
         }
         // Prepare the buffer for reading
         outputBuffer.flip();
         int result = super.writeBlocking(outputBuffer, timeRemaining, TimeUnit.MILLISECONDS);
         if (result == -1) return -1;
         numBufWritten += res.bytesConsumed();
         timeRemaining -= Math.max(-now + (now = System.currentTimeMillis()), 0L);
      }
      return numBufWritten;
   }

}
