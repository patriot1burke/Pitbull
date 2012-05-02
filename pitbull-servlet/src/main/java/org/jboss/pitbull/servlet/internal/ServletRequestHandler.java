package org.jboss.pitbull.servlet.internal;

import org.jboss.pitbull.Connection;
import org.jboss.pitbull.RequestHeader;
import org.jboss.pitbull.handlers.stream.StreamRequestHandler;
import org.jboss.pitbull.handlers.stream.StreamedResponse;
import org.jboss.pitbull.internal.logging.Logger;
import org.jboss.pitbull.servlet.DeploymentServletContext;
import org.jboss.pitbull.servlet.DeploymentServletRegistration;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ServletRequestHandler implements StreamRequestHandler
{
   protected DeploymentServletRegistration servlet;
   protected DeploymentServletContext context;
   protected static final Logger log = Logger.getLogger(ServletRequestHandler.class);

   public ServletRequestHandler(DeploymentServletRegistration servlet, DeploymentServletContext context)
   {
      this.servlet = servlet;
      this.context = context;
   }

   @Override
   public void execute(Connection connection, RequestHeader requestHeader, InputStream input, StreamedResponse writer) throws IOException
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
      catch (IOException e)
      {
         throw e;
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

}
