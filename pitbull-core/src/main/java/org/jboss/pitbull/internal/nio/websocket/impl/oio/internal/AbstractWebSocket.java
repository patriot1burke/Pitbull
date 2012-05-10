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

package org.jboss.pitbull.internal.nio.websocket.impl.oio.internal;

import org.jboss.pitbull.internal.nio.websocket.impl.oio.ClosingStrategy;
import org.jboss.pitbull.internal.nio.websocket.impl.oio.OioWebSocket;
import org.jboss.pitbull.internal.nio.websocket.impl.oio.internal.util.Assert;
import org.jboss.pitbull.internal.nio.websocket.impl.oio.internal.util.Hash;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * @author Mike Brock
 */
public abstract class AbstractWebSocket implements OioWebSocket
{
   protected final String webSocketId;
   protected final URI uri;
   protected final InputStream inputStream;
   protected final OutputStream outputStream;
   protected final ClosingStrategy closingStrategy;
   protected final String webSocketVersion;

   protected AbstractWebSocket(
           final String version,
           final URI uri,
           final InputStream inputStream,
           final OutputStream outputStream,
           final ClosingStrategy closingStrategy)
   {
      this.webSocketVersion = version;
      this.uri = uri;
      this.webSocketId = Hash.newUniqueHash();
      this.inputStream = Assert.notNull(inputStream, "inputStream must NOT be null");
      this.outputStream = Assert.notNull(outputStream, "outputStream must NOT be null");
      this.closingStrategy = Assert.notNull(closingStrategy, "closingStrategy must NOT be null");
   }

   @Override
   public URI getUri()
   {
      return uri;
   }

   @Override
   public final String getSocketID()
   {
      return webSocketId;
   }

   @Override
   public void closeSocket() throws IOException
   {
      closingStrategy.doClose();
   }

   @Override
   public String getVersion()
   {
      return webSocketVersion;
   }
}
