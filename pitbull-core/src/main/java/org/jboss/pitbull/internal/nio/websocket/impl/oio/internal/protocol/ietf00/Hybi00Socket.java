/*
 * Copyright 2012 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.pitbull.internal.nio.websocket.impl.oio.internal.protocol.ietf00;

import org.jboss.pitbull.internal.nio.websocket.impl.Frame;
import org.jboss.pitbull.internal.nio.websocket.impl.FrameType;
import org.jboss.pitbull.internal.nio.websocket.impl.frame.TextFrame;
import org.jboss.pitbull.internal.nio.websocket.impl.oio.ClosingStrategy;
import org.jboss.pitbull.internal.nio.websocket.impl.oio.internal.AbstractWebSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * The Hybi-00 Framing Protocol implementation.
 *
 * @author Mike Brock
 * @see org.jboss.pitbull.internal.nio.websocket.impl.oio.internal.protocol.ietf00.Hybi00Handshake
 */
public class Hybi00Socket extends AbstractWebSocket
{
  private final static int MAX_FRAME_SIZE = 1024 * 32; //32kb


  protected Hybi00Socket(
                       final String version,
                       final URI uri,
                       final InputStream inputStream,
                       final OutputStream outputStream,
                       final ClosingStrategy closingStrategy) {
    super(version, uri, inputStream, outputStream, closingStrategy);
  }

  public void writeTextFrame(final String text) throws IOException {
    outputStream.write(0x00);
    outputStream.write(text.getBytes("UTF-8"));
    outputStream.write((byte) 0xFF);
    outputStream.flush();
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  private String _readTextFrame() throws IOException {
    byte frametype = (byte) inputStream.read();

    if ((frametype & 0x80) == 0x80) {
      throw new RuntimeException("binary payload not supported");
    }
    else if (frametype == 0) {
      final StringBuilder buf = new StringBuilder();
      int b;
      int read = 0;

      while ((b = inputStream.read()) != 0xFF) {
        if (++read > MAX_FRAME_SIZE) {
          throw new RuntimeException("frame too large");
        }
        buf.append((char) b);
      }

      return buf.toString();
    }
    else {
      throw new RuntimeException("bad websockets payload");
    }
  }

  public void writeFrame(Frame frame) throws IOException {
    if (frame.getType() == FrameType.Text) {
      writeTextFrame(((TextFrame) frame).getText());
    }
     else
    {
       throw new IOException("unable to handle frame type: " + frame.getType());
    }
  }

  public Frame readFrame() throws IOException {
    return TextFrame.from(_readTextFrame());
  }
}
