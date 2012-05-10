package org.jboss.pitbull.client;

import org.jboss.pitbull.OrderedHeaders;
import org.jboss.pitbull.internal.NotImplementedYetException;
import org.jboss.pitbull.internal.client.websocket.protocol.ietf13.Hybi13WebSocketBuilder;
import org.jboss.pitbull.util.OrderedHeadersImpl;
import org.jboss.pitbull.websocket.WebSocket;
import org.jboss.pitbull.websocket.WebSocketVersion;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class WebSocketBuilder
{
   protected URI uri;
   protected String host;
   protected int port;
   protected String path;
   protected String origin;
   protected String protocol;
   protected OrderedHeaders headers = new OrderedHeadersImpl();
   protected boolean secured;

   /**
    * Creates a websocket of latest supported protocol
    *
    * @return
    */
   public static WebSocketBuilder create()
   {
      return new Hybi13WebSocketBuilder();
   }

   public static WebSocketBuilder create(WebSocketVersion version)
   {
      throw new NotImplementedYetException();
   }

   protected WebSocketBuilder uri(String uriString) throws URISyntaxException
   {
      URI uri = new URI(uriString);
      return uri(uri);
   }

   protected WebSocketBuilder uri(URI uri) throws URISyntaxException
   {
      this.uri  = uri;
      if (uri.getScheme().equals("ws"))
      {
         secured = false;
         port = 80;
      }
      else if (uri.getScheme().equals("wss"))
      {
         secured = true;
         port = 443;
      }
      else
      {
         throw new URISyntaxException(uri.toString(), "Must have 'ws' or 'wss' as URI scheme");
      }
      host = uri.getHost();
      if (uri.getPort() != -1)
      {
         port = uri.getPort();
      }
      this.path = uri.getRawPath();
      if (uri.getRawQuery() != null)
      {
         this.path += "?" + uri.getRawQuery();
      }
      if (uri.getRawFragment() != null)
      {
         this.path += "#" + uri.getRawFragment();
      }
      return this;
   }

   /**
    * Origin defines where the request is coming from.  Some websocket servers may require this setting.
    *
    * see definition at {@link <a href="http://tools.ietf.org/html/rfc6454#section-7">HTTP Origin Header</a>}
    *
    * @param origin
    * @return
    */
   public WebSocketBuilder origin(String origin)
   {
      this.origin = origin;
      return this;
   }

   /**
    * Websocket application protocol you desire to use.
    *
    * @param protocol
    * @return
    */
   public WebSocketBuilder protocol(String protocol)
   {
      this.protocol = protocol;
      return this;
   }

   /**
    * HTTP header you want to send with the initial handshake request
    *
    * @param name
    * @param value
    * @return
    */
   public WebSocketBuilder header(String name, String value)
   {
      headers.addHeader(name, value);
      return this;
   }

   /**
    * URI of the form of ws[s]://host[:port]/path[?query][#fragment]
    *
    * "ws" scheme is an vanilla socket connection.
    * "wss" is over SSL.
    *
    *
    * @param uri
    * @return
    * @throws URISyntaxException
    */
   public WebSocket connect(String uri) throws URISyntaxException, IOException
   {
      uri(uri);
      return doConnect();
   }

   /**
    * URI of the form of ws[s]://host[:port]/path[?query][#fragment]
    *
    * "ws" scheme is an vanilla socket connection.
    * "wss" is over SSL.
    *
    *
    * @param uri
    * @return
    * @throws URISyntaxException
    */
   public WebSocket connect(URI uri) throws URISyntaxException, IOException
   {
      uri(uri);
      return doConnect();
   }

   protected abstract WebSocket doConnect() throws IOException;
}
