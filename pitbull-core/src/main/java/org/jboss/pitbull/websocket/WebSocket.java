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

package org.jboss.pitbull.websocket;

import org.jboss.pitbull.Connection;

import java.io.IOException;
import java.net.URI;

/**
 * A blocking interface for reading and writing WebSocket frames.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 */
public interface WebSocket
{
   public Connection getConnection();

   /**
    * URI used to connect to this websocket
    *
    * @return
    */
   public URI getUri();

   public String getVersion();
   /**
    * Read a frame.  This may block.
    *
    * @return
    */
   public Frame readFrame() throws IOException;

   /**
    * Write an text frame to the websocket. All String data will be UTF-8 encoded on the wire.
    *
    * @param text the UTF-8 text string
    * @throws java.io.IOException
    */
   public void writeTextFrame(String text) throws IOException;

   public void writeBinaryFrame(byte[] bytes) throws IOException;

   public void writeCloseFrame() throws IOException;

   public void writePongFrame() throws IOException;

   public void writePingFrame() throws IOException;

   /**
    * Terminates the connection with the client and closes the socket.
    */
   public void close() throws IOException;

   public boolean isClosed();
}
