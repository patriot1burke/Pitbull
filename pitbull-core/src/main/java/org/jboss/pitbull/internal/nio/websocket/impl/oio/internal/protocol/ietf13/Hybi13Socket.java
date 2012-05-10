package org.jboss.pitbull.internal.nio.websocket.impl.oio.internal.protocol.ietf13;

import org.jboss.pitbull.internal.nio.websocket.impl.Frame;
import org.jboss.pitbull.internal.nio.websocket.impl.FrameType;
import org.jboss.pitbull.internal.nio.websocket.impl.frame.BinaryFrame;
import org.jboss.pitbull.internal.nio.websocket.impl.frame.CloseFrame;
import org.jboss.pitbull.internal.nio.websocket.impl.frame.PingFrame;
import org.jboss.pitbull.internal.nio.websocket.impl.frame.PongFrame;
import org.jboss.pitbull.internal.nio.websocket.impl.frame.TextFrame;
import org.jboss.pitbull.internal.nio.websocket.impl.oio.ClosingStrategy;
import org.jboss.pitbull.internal.nio.websocket.impl.oio.internal.AbstractWebSocket;
import org.jboss.pitbull.internal.nio.websocket.impl.oio.internal.util.Hash;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Random;

/**
 * @author Mike Brock
 */
public class Hybi13Socket extends AbstractWebSocket
{
   final private boolean isMaskedWrites;

   public Hybi13Socket(final String version,
                       final URI uri,
                       final InputStream inputStream,
                       final OutputStream outputStream,
                       final ClosingStrategy closingStrategy,
                       final boolean isMaskedWrites)
   {
      super(version, uri, inputStream, outputStream, closingStrategy);
      this.isMaskedWrites = isMaskedWrites;
   }

   private static final byte FRAME_OPCODE = 127;
   private static final byte FRAME_MASKED = Byte.MIN_VALUE;
   private static final byte FRAME_LENGTH = 127;

   private FrameType getNextFrameType() throws IOException
   {
      switch ((inputStream.read() & FRAME_OPCODE))
      {
         case 0x00:
            return FrameType.Continuation;
         case 0x01:
            return FrameType.Text;
         case 0x02:
            return FrameType.Binary;
         case 0x08:
            return FrameType.ConnectionClose;
         case 0x09:
            return FrameType.Ping;
         case 0x0A:
            return FrameType.Pong;
         default:
            return FrameType.Unknown;
      }
   }

   private int getPayloadSize(int b) throws IOException
   {
      int payloadLength = (b & FRAME_LENGTH);
      if (payloadLength == 126)
      {
         payloadLength = ((inputStream.read() & 0xFF) << 8) +
                 (inputStream.read() & 0xFF);
      }
      else if (payloadLength == 127)
      {
         // ignore the first 4-bytes. We can't deal with 64-bit ints right now anyways.
         inputStream.read();
         inputStream.read();
         inputStream.read();
         inputStream.read();
         payloadLength = ((inputStream.read() & 0xFF) << 24) +
                 ((inputStream.read() & 0xFF) << 16) +
                 ((inputStream.read() & 0xFF) << 8) +
                 ((inputStream.read() & 0xFF));
      }

      return payloadLength;
   }


   @SuppressWarnings("ResultOfMethodCallIgnored")
   private String _readTextFrame() throws IOException
   {
      int b = inputStream.read();
      final boolean frameMasked = (b & FRAME_MASKED) != 0;
      int payloadLength = getPayloadSize(b);

      final byte[] frameMaskingKey = new byte[4];

      if (frameMasked)
      {
         inputStream.read(frameMaskingKey);
      }

      final StringBuilder payloadBuffer = new StringBuilder(payloadLength);

      int read = 0;
      if (frameMasked)
      {
         do
         {
            payloadBuffer.append(((char) ((inputStream.read() ^ frameMaskingKey[read % 4]) & 127)));
         }
         while (++read < payloadLength);
      }
      else
      {
         // support unmasked frames for testing.

         do
         {
            payloadBuffer.append((char) inputStream.read());
         }
         while (++read < payloadLength);
      }

      return payloadBuffer.toString();
   }

   @SuppressWarnings("ResultOfMethodCallIgnored")
   public byte[] _readBinaryFrame() throws IOException
   {
      int b = inputStream.read();
      final boolean frameMasked = (b & FRAME_MASKED) != 0;
      int payloadLength = getPayloadSize(b);

      final byte[] frameMaskingKey = new byte[4];

      if (frameMasked)
      {
         inputStream.read(frameMaskingKey);
      }


      final byte[] buf = new byte[payloadLength];

      int read = 0;
      if (frameMasked)
      {
         do
         {
            buf[read] = (byte) ((inputStream.read() ^ frameMaskingKey[read % 4]));
         }
         while (++read < payloadLength);
      }
      else
      {
         // support unmasked frames for testing.

         do
         {
            buf[read] = (byte) inputStream.read();
         }
         while (++read < payloadLength);
      }

      return buf;
   }

   private void _writeTextFrame(final String txt) throws IOException
   {
      outputStream.write(-127);
      byte[] bytes = txt.getBytes("UTF-8");
      writeData(bytes, isMaskedWrites);
   }

   private void writeData(byte[] bytes, boolean maskedWrites) throws IOException
   {
      final int len = bytes.length;
      if (bytes.length > Short.MAX_VALUE)
      {
         if (maskedWrites)
         {
            outputStream.write(0xFF);
         }
         else
         {
            outputStream.write(127);
         }

         // pad the first 4 bytes of 64-bit context length. If this frame is larger than 2GB, you're in trouble. =)
         outputStream.write(0);
         outputStream.write(0);
         outputStream.write(0);
         outputStream.write(0);
         outputStream.write((len & 0xFF) << 24);
         outputStream.write((len & 0xFF) << 16);
         outputStream.write((len & 0xFF) << 8);
         outputStream.write((len & 0xFF));
      }
      else if (bytes.length > 125)
      {
         if (maskedWrites)
         {
            outputStream.write(0xFE);
         }
         else
         {
            outputStream.write(126);
         }
         outputStream.write(((len >> 8) & 0xFF));
         outputStream.write(((len) & 0xFF));
      }
      else
      {
         if (maskedWrites)
         {
            outputStream.write(((len & 127) | 0x80));
         }
         else
         {
            outputStream.write((len & 127));
         }
      }


      if (maskedWrites)
      {
         byte[] mask = new byte[4];
         Hash.getRandomBytes(mask);
         outputStream.write(mask);
         int mod = 0;
         for (byte strByte : bytes)
         {
            outputStream.write(strByte ^ mask[mod++ % 4]);
         }
      }
      else
      {
         for (byte strByte : bytes)
         {
            outputStream.write(strByte);
         }
      }

      outputStream.flush();
   }

   private void _writeBinaryFrame(final byte[] data) throws IOException
   {

      outputStream.write(-126);
      writeData(data, isMaskedWrites);
   }

   private void _sendConnectionClose() throws IOException
   {
      outputStream.write(-120);
      outputStream.write(125);
      outputStream.flush();
   }

   private void _sendPing() throws IOException
   {
      outputStream.write(-119);
      outputStream.write(125);
      outputStream.flush();
   }

   private void _sendPong() throws IOException
   {
      outputStream.write(-118);
      outputStream.write(125);
      outputStream.flush();
   }

   public Frame readFrame() throws IOException
   {
      switch (getNextFrameType())
      {
         case Text:
            return TextFrame.from(_readTextFrame());
         case Binary:
            return BinaryFrame.from(_readBinaryFrame());
         case Ping:
            return new PingFrame();
         case Pong:
            return new PongFrame();
         case ConnectionClose:
            closeSocket();
            return new CloseFrame();
      }
      throw new IOException("unknown frame type");
   }

   public void writeFrame(Frame frame) throws IOException
   {
      switch (frame.getType())
      {
         case Text:
            _writeTextFrame(((TextFrame) frame).getText());
            break;
         case Binary:
            _writeBinaryFrame(((BinaryFrame) frame).getByteArray());
            break;
         case ConnectionClose:
            _sendConnectionClose();
            break;
         case Ping:
            _sendPing();
            break;
         case Pong:
            _sendPong();
            break;
         default:
            throw new IOException("unable to handle frame type: " + frame.getType());
      }
   }
}