package org.jboss.pitbull.client;


import org.jboss.pitbull.handlers.stream.ContentOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Future;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ClientInvocation
{
   ClientInvocation header(String name, String value);
   ClientInvocation get();
   ClientInvocation put();
   ClientInvocation post();
   ClientInvocation delete();
   ClientInvocation method(String method);

   /**
    * Request headers are not written to socket until the stream is first flushed or it is closed.
    * If Content-Length is not set, this stream will use chunked transfer encoding if the buffer is flushed.
    * If Content-Length is set and you write more bytes than the Content-Length header an exception will be thrown
    * from a write call.
    *
    * @return
    */
   ContentOutputStream getRequestBody();

   /**
    * Blocks until at least the response code and headers are available for reading.
    *
    * @return
    */
   ClientResponse response() throws IOException;

   /**
    * Like response() Future is not complete until at least the response code and headers are available for reading
    *
    * @return
    */
   Future<ClientResponse> asyncResponse();




}
