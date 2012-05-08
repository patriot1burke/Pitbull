/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.pitbull.internal.nio.socket;

import org.jboss.pitbull.internal.logging.Logger;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @version $Rev: 2200 $, $Date: 2010-02-23 14:42:39 +0900 (Tue, 23 Feb 2010) $
 */
public final class SelectorUtil
{
   protected static final Logger logger = Logger.getLogger(SelectorUtil.class);

   public static void safeClose(Selector selector)
   {
      try
      { selector.close(); }
      catch (Throwable ignored)
      {}
   }

   public static void select(Selector selector) throws IOException
   {
      try
      {
         selector.select(500);
      }
      catch (CancelledKeyException e)
      {
         // Harmless exception - log anyway
         logger.trace(
                 CancelledKeyException.class.getSimpleName() +
                         " raised by a Selector - JDK bug?", e);
      }
   }

   private interface SelectorCreator
   {
      Selector open() throws IOException;
   }

   private final static SelectorCreator selectorCreator;

   static
   {
      final String providerClassName = SelectorProvider.provider().getClass().getCanonicalName();
      if ("sun.nio.ch.PollSelectorProvider".equals(providerClassName))
      {
         logger.warn("The currently defined selector provider class (%s) is not supported for use with PitBull", providerClassName);
      }
      logger.trace("Starting up with selector provider %s", providerClassName);
      selectorCreator = AccessController.doPrivileged(
              new PrivilegedAction<SelectorCreator>()
              {
                 public SelectorCreator run()
                 {
                    try
                    {
                       // A Polling selector is most efficient on most platforms for one-off selectors.  Try to hack a way to get them on demand.
                       final Class<? extends Selector> selectorImplClass = Class.forName("sun.nio.ch.PollSelectorImpl").asSubclass(Selector.class);
                       final Constructor<? extends Selector> constructor = selectorImplClass.getDeclaredConstructor(SelectorProvider.class);
                       // Usually package private.  So untrusting.
                       constructor.setAccessible(true);
                       logger.trace("Using polling selector type for temporary selectors.");
                       return new SelectorCreator()
                       {
                          public Selector open() throws IOException
                          {
                             try
                             {
                                return constructor.newInstance(SelectorProvider.provider());
                             }
                             catch (InstantiationException e)
                             {
                                return Selector.open();
                             }
                             catch (IllegalAccessException e)
                             {
                                return Selector.open();
                             }
                             catch (InvocationTargetException e)
                             {
                                try
                                {
                                   throw e.getTargetException();
                                }
                                catch (IOException e2)
                                {
                                   throw e2;
                                }
                                catch (RuntimeException e2)
                                {
                                   throw e2;
                                }
                                catch (Error e2)
                                {
                                   throw e2;
                                }
                                catch (Throwable t)
                                {
                                   throw new IllegalStateException("Unexpected invocation exception", t);
                                }
                             }
                          }
                       };
                    }
                    catch (Exception e)
                    {
                       // ignore.
                    }
                    // Can't get our selector type?  That's OK, just use the default.
                    logger.trace("Using default selector type for temporary selectors.");
                    return new SelectorCreator()
                    {
                       public Selector open() throws IOException
                       {
                          return Selector.open();
                       }
                    };
                 }
              }
      );

   }

   private static final ThreadLocal<Selector> selectorThreadLocal = new ThreadLocal<Selector>()
   {
      public void remove()
      {
         // if no selector was created, none will be closed
         if (get() != null)
         {
            try
            { get().close(); }
            catch (Throwable ignored)
            {}
         }
         super.remove();
      }
   };

   public static void cleanupThreadSelector()
   {
      selectorThreadLocal.remove();
   }


   private static Selector getSelector() throws IOException
   {
      final ThreadLocal<Selector> threadLocal = selectorThreadLocal;
      Selector selector = threadLocal.get();
      if (selector == null)
      {
         selector = selectorCreator.open();
         threadLocal.set(selector);
      }
      return selector;
   }

   public static void awaitReadable(SelectableChannel channel) throws IOException
   {
      await(channel, SelectionKey.OP_READ);
   }

   public static void awaitReadable(SelectableChannel channel, long time, TimeUnit unit) throws IOException
   {
      await(channel, SelectionKey.OP_READ, time, unit);

   }

   public static void awaitWritable(SelectableChannel channel) throws IOException
   {
      await(channel, SelectionKey.OP_WRITE);
   }

   public static void awaitWritable(SelectableChannel channel, long time, TimeUnit unit) throws IOException
   {
      await(channel, SelectionKey.OP_WRITE, time, unit);

   }

   public static void await(SelectableChannel channel, int op) throws IOException
   {
      final Selector selector = getSelector();
      final SelectionKey selectionKey = channel.register(selector, op);
      selector.select();
      if (Thread.currentThread().isInterrupted())
      {
         throw new InterruptedIOException();
      }
      selectionKey.interestOps(0);
   }

   public static void await(SelectableChannel channel, int op, long time, TimeUnit unit) throws IOException
   {
      final Selector selector = getSelector();
      final SelectionKey selectionKey = channel.register(selector, op);
      selector.select(unit.toMillis(time));
      if (Thread.currentThread().isInterrupted())
      {
         throw new InterruptedIOException();
      }
      selectionKey.interestOps(0);
   }
}
