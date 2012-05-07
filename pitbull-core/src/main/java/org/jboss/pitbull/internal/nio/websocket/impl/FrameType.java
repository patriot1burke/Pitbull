package org.jboss.pitbull.internal.nio.websocket.impl;

/**
 * @author Mike Brock
 */
public enum FrameType {
  Continuation, Text, Binary, Ping, Pong, ConnectionClose, Unknown
}
