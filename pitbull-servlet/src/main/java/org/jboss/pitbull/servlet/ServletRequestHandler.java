package org.jboss.pitbull.servlet;

import org.jboss.pitbull.logging.Logger;
import org.jboss.pitbull.spi.Connection;
import org.jboss.pitbull.spi.InputStreamHandler;
import org.jboss.pitbull.spi.OutputStreamHandler;
import org.jboss.pitbull.spi.RequestHandler;
import org.jboss.pitbull.spi.RequestHeader;
import org.jboss.pitbull.spi.StreamResponseWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ServletRequestHandler implements InputStreamHandler, OutputStreamHandler, RequestHandler
{
   protected InputStream input;
   protected StreamResponseWriter writer;
   protected Connection connection;
   protected Servlet servlet;
   protected static final Logger log = Logger.getLogger(ServletRequestHandler.class);

   public ServletRequestHandler(Connection connection, Servlet servlet)
   {
      this.connection = connection;
      this.servlet = servlet;
   }

   @Override
   public void setInputStream(InputStream input)
   {
      this.input = input;
   }

   @Override
   public void setWriter(StreamResponseWriter writer)
   {
      this.writer = writer;
   }

   @Override
   public void execute(RequestHeader requestHeader)
   {
      HttpServletRequestImpl request = new HttpServletRequestImpl();
      request.setConnection(connection);
      request.setHeaderBlob(requestHeader);
      request.setIs(input);
      HttpServletResponseImpl response = new HttpServletResponseImpl(writer);
      HttpServlet instance = servlet.getServletInstance();
      try
      {
         instance.service(request, response);
      }
      catch (Throwable e)
      {
         log.error("Failure executing servlet", e);
         if (writer.getAllocatedStream() == null || !writer.getAllocatedStream().isCommitted())
         {
            response.reset();
            response.setStatus(500);
         }
      }
      writer.end(response);
   }

   @Override
   public void cancel()
   {
   }
}
