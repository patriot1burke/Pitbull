package org.jboss.pitbull.servlet.internal;

import org.jboss.pitbull.internal.logging.Logger;
import org.jboss.pitbull.servlet.DeploymentServletContext;
import org.jboss.pitbull.servlet.DeploymentServletRegistration;
import org.jboss.pitbull.spi.Connection;
import org.jboss.pitbull.spi.RequestHeader;
import org.jboss.pitbull.spi.StreamHandler;
import org.jboss.pitbull.spi.StreamResponseWriter;

import javax.servlet.http.HttpServlet;
import java.io.InputStream;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ServletRequestHandler implements StreamHandler
{
   protected InputStream input;
   protected StreamResponseWriter writer;
   protected Connection connection;
   protected DeploymentServletRegistration servlet;
   protected DeploymentServletContext context;
   protected static final Logger log = Logger.getLogger(ServletRequestHandler.class);

   public ServletRequestHandler(Connection connection, DeploymentServletRegistration servlet, DeploymentServletContext context)
   {
      this.connection = connection;
      this.servlet = servlet;
      this.context = context;
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
      request.setContext(context);
      HttpServletResponseImpl response = new HttpServletResponseImpl(writer);
      try
      {
         HttpServlet instance = (HttpServlet) servlet.startRequest();
         try
         {
            instance.service(request, response);
         }
         finally
         {
            servlet.endRequest();
         }
      }
      catch (Throwable e)
      {
         log.error("Failure executing servlet", e);
         if (writer.getAllocatedStream() == null || !writer.getAllocatedStream().isCommitted())
         {
            response.reset();
            response.setStatus(500);
         }
         else
         {
            throw new RuntimeException(e);
         }
      }
      writer.end(response);
   }

   @Override
   public boolean canExecuteInWorkerThread()
   {
      return false;
   }

   @Override
   public void unsupportedHandler()
   {

   }
}
