package org.jboss.pitbull.internal.nio.websocket;

import org.jboss.pitbull.Connection;
import org.jboss.pitbull.RequestHeader;
import org.jboss.pitbull.websocket.BinaryFrame;
import org.jboss.pitbull.websocket.CloseFrame;
import org.jboss.pitbull.websocket.Frame;
import org.jboss.pitbull.websocket.PingFrame;
import org.jboss.pitbull.websocket.PongFrame;
import org.jboss.pitbull.websocket.TextFrame;
import org.jboss.pitbull.websocket.WebSocket;
import org.jboss.pitbull.internal.nio.websocket.impl.oio.OioWebSocket;

import java.io.IOException;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class WebSocketImpl implements WebSocket
{
   protected OioWebSocket oioWebSocket;
   protected Connection connection;
   protected RequestHeader requestHeader;
   protected boolean closed;

   public WebSocketImpl(Connection connection, OioWebSocket oioWebSocket, RequestHeader requestHeader)
   {
      this.connection = connection;
      this.oioWebSocket = oioWebSocket;
      this.requestHeader = requestHeader;
   }

   @Override
   public Connection getConnection()
   {
      return connection;
   }

   @Override
   public RequestHeader getRequestHeader()
   {
      return requestHeader;
   }

   @Override
   public Frame readFrame() throws IOException
   {
      org.jboss.pitbull.internal.nio.websocket.impl.Frame frame = oioWebSocket.readFrame();
      switch (frame.getType()) {
        case Text:
        {
           final org.jboss.pitbull.internal.nio.websocket.impl.frame.TextFrame textFrame = (org.jboss.pitbull.internal.nio.websocket.impl.frame.TextFrame)frame;
           return new TextFrame() {
              @Override
              public String getText()
              {
                 return textFrame.getText();
              }

              @Override
              public String getEncoding()
              {
                 return "UTF-8";
              }
           };
        }
        case Binary:
        {
           final org.jboss.pitbull.internal.nio.websocket.impl.frame.BinaryFrame binaryFrame = (org.jboss.pitbull.internal.nio.websocket.impl.frame.BinaryFrame)frame;
           return new BinaryFrame() {
              @Override
              public byte[] getBytes()
              {
                 return binaryFrame.getByteArray();
              }
           };
        }
        case ConnectionClose:
          return new CloseFrame()
          {
          };
        case Ping:
          return new PingFrame()
          {
          };
        case Pong:
          return new PongFrame()
          {
          };
        default:
          throw new IOException("unable to handle frame type: " + frame.getType());
      }
   }

   @Override
   public void writeTextFrame(String text) throws IOException
   {
      oioWebSocket.writeFrame(org.jboss.pitbull.internal.nio.websocket.impl.frame.TextFrame.from(text));
   }

   @Override
   public void writeBinaryFrame(byte[] bytes) throws IOException
   {
      oioWebSocket.writeFrame(org.jboss.pitbull.internal.nio.websocket.impl.frame.BinaryFrame.from(bytes));
   }

   @Override
   public void writeCloseFrame() throws IOException
   {
      oioWebSocket.writeFrame(new org.jboss.pitbull.internal.nio.websocket.impl.frame.CloseFrame());
   }

   @Override
   public void writePongFrame() throws IOException
   {
      oioWebSocket.writeFrame(new org.jboss.pitbull.internal.nio.websocket.impl.frame.PongFrame());
   }

   @Override
   public void writePingFrame() throws IOException
   {
      oioWebSocket.writeFrame(new org.jboss.pitbull.internal.nio.websocket.impl.frame.PingFrame());
   }

   @Override
   public void close() throws IOException
   {
      if (closed) return;
      closed = true;
      oioWebSocket.closeSocket();
   }

   @Override
   public boolean isClosed()
   {
      return closed;
   }
}
