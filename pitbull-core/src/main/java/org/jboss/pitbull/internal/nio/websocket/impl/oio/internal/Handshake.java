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
import org.jboss.pitbull.internal.nio.websocket.impl.oio.HttpRequestBridge;
import org.jboss.pitbull.internal.nio.websocket.impl.oio.HttpResponseBridge;
import org.jboss.pitbull.internal.nio.websocket.impl.oio.OioWebSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * @author Mike Brock
 */
public abstract class Handshake
{
   private final String version;
   private final String hashAlgorithm;
   private final String magicNumber;

   public Handshake(String version, String hashAlgorithm, String magicNumber)
   {
      this.version = version;
      this.hashAlgorithm = hashAlgorithm;
      this.magicNumber = magicNumber;
   }

   public String getVersion()
   {
      return this.version;
   }

   public String getHashAlgorithm()
   {
      return hashAlgorithm;
   }

   public String getMagicNumber()
   {
      return magicNumber;
   }

   protected String getWebSocketLocation(HttpRequestBridge request)
   {
      StringBuilder builder = new StringBuilder(100);
      if (request.isSecure())
      {
         builder.append("wss://");
      }
      else
      {
         builder.append("ws://");
      }
      builder.append(request.getHeader("Host")).append(request.getRequestURI());
      return builder.toString();
   }

   public abstract OioWebSocket getClientWebSocket(URI uri,
                                                   InputStream inputStream,
                                                   OutputStream outputStream,
                                                   ClosingStrategy closingStrategy) throws IOException;

   public abstract OioWebSocket getServerWebSocket(HttpRequestBridge request,
                                                   HttpResponseBridge response,
                                                   ClosingStrategy closingStrategy) throws IOException;

   public abstract boolean matches(HttpRequestBridge request);

   public abstract byte[] generateResponse(HttpRequestBridge request,
                                           HttpResponseBridge response) throws IOException;
}
