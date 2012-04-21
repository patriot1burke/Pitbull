package org.jboss.pitbull.servlet.internal;

import org.jboss.pitbull.NotImplementedYetException;
import org.jboss.pitbull.spi.ContentOutputStream;
import org.jboss.pitbull.spi.OrderedHeaders;
import org.jboss.pitbull.spi.ResponseHeader;
import org.jboss.pitbull.spi.StreamResponseWriter;
import org.jboss.pitbull.util.CaseInsensitiveMap;
import org.jboss.pitbull.util.OrderedHeadersImpl;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HttpServletResponseImpl implements HttpServletResponse, ResponseHeader
{
   protected OrderedHeaders headers = new OrderedHeadersImpl();
   protected int status;
   protected String statusMessage;
   protected boolean committed;
   protected ContentOutputStream underlyingStream;
   protected StreamResponseWriter streamResponseWriter;

   public HttpServletResponseImpl(StreamResponseWriter streamResponseWriter)
   {
      this.streamResponseWriter = streamResponseWriter;
   }

   @Override
   public String getStatusMessage()
   {
      return statusMessage;
   }

   @Override
   public OrderedHeaders getHeaders()
   {
      return headers;
   }

   @Override
   public void addCookie(Cookie cookie)
   {
      StringBuffer buf = new StringBuffer();
      ServerCookie.appendCookieValue(buf, cookie.getVersion(), cookie.getName(), cookie.getValue(), cookie.getPath(),
              cookie.getDomain(), cookie.getComment(), cookie.getMaxAge(), cookie.getSecure(), cookie.isHttpOnly());
      addHeader("Set-Cookie", buf.toString());
   }

   @Override
   public boolean containsHeader(String name)
   {
      return headers.containsHeader(name);
   }

   @Override
   public String encodeURL(String url)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public String encodeRedirectURL(String url)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public String encodeUrl(String url)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public String encodeRedirectUrl(String url)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public void sendError(int sc, String msg) throws IOException
   {
      if (isCommitted())
      {
         throw new IllegalStateException("Response is committed");
      }
      setStatus(sc);
      headers.clear();
      setContentType("text/html");
      getUnderlyingStream().reset();
      StringBuilder builder = new StringBuilder("<html><body><h1>Server Error</h1>");
      builder.append("<p>Error code: ").append(sc).append("</p><p>");
      builder.append(msg);
      builder.append("</p></body></html>");
      getUnderlyingStream().write(builder.toString().getBytes());
   }

   @Override
   public void sendError(int sc) throws IOException
   {
      // todo error page.html

      if (isCommitted())
      {
         throw new IllegalStateException("Response is committed");
      }
      setStatus(sc);
      headers.clear();
      setContentType("text/html");
      getUnderlyingStream().reset();
      StringBuilder builder = new StringBuilder("<html><body><h1>Server Error</h1>");
      builder.append("<p>Error code: ").append(sc).append("</p><p>");
      builder.append("</p></body></html>");
      getUnderlyingStream().write(builder.toString().getBytes());
   }

   @Override
   public void sendRedirect(String location) throws IOException
   {
      throw new NotImplementedYetException();
   }

   @Override
   public void setDateHeader(String name, long date)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public void addDateHeader(String name, long date)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public void setHeader(String name, String value)
   {
      headers.setHeader(name, value);
   }

   private static class HeaderEntry implements Map.Entry<String, String>
   {
      private String name;
      private String value;

      private HeaderEntry(String name, String value)
      {
         this.name = name;
         this.value = value;
      }

      @Override
      public String getKey()
      {
         return name;
      }

      @Override
      public String getValue()
      {
         return value;
      }

      @Override
      public String setValue(String s)
      {
         return value = s;
      }
   }

   @Override
   public void addHeader(String name, String value)
   {
      headers.addHeader(name, value);
   }


   @Override
   public void setIntHeader(String name, int value)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public void addIntHeader(String name, int value)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public void setStatus(int sc)
   {
      this.status = sc;
   }

   @Override
   public void setStatus(int sc, String sm)
   {
      this.status = sc;
      this.statusMessage = sm;
   }

   @Override
   public int getStatus()
   {
      return status;
   }

   @Override
   public String getHeader(String name)
   {
      return headers.getFirstHeader(name);
   }

   @Override
   public Collection<String> getHeaders(String name)
   {
      return headers.getHeaderValues(name);
   }

   @Override
   public Collection<String> getHeaderNames()
   {
      return headers.getHeaderNames();
   }

   @Override
   public String getCharacterEncoding()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public String getContentType()
   {
      throw new NotImplementedYetException();
   }

   protected ContentOutputStream getUnderlyingStream()
   {
      if (underlyingStream == null) underlyingStream = streamResponseWriter.getStream(this);
      return underlyingStream;
   }

   @Override
   public ServletOutputStream getOutputStream() throws IOException
   {

      return new ServletOutputStream()
      {
         @Override
         public void write(byte[] bytes) throws IOException
         {
            getUnderlyingStream().write(bytes);
         }

         @Override
         public void write(byte[] bytes, int i, int i1) throws IOException
         {
            getUnderlyingStream().write(bytes, i, i1);
         }

         @Override
         public void flush() throws IOException
         {
            getUnderlyingStream().flush();
         }

         @Override
         public void close() throws IOException
         {
            getUnderlyingStream().close();
            super.close();
         }

         @Override
         public void write(int i) throws IOException
         {
            getUnderlyingStream().write(i);
         }
      };
   }

   @Override
   public PrintWriter getWriter() throws IOException
   {
      throw new NotImplementedYetException();
   }

   @Override
   public void setCharacterEncoding(String charset)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public void setContentLength(int len)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public void setContentType(String type)
   {
      setHeader("Content-Type", type);
   }

   @Override
   public void setBufferSize(int size)
   {
      getUnderlyingStream().setBufferSize(size);
   }

   @Override
   public int getBufferSize()
   {
      return getUnderlyingStream().getBufferSize();
   }

   @Override
   public void flushBuffer() throws IOException
   {
      getUnderlyingStream().flush();
   }

   @Override
   public void resetBuffer()
   {
      getUnderlyingStream().reset();
   }

   @Override
   public boolean isCommitted()
   {
      return committed;
   }

   @Override
   public void reset()
   {
      getUnderlyingStream().reset();
   }

   @Override
   public void setLocale(Locale loc)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public Locale getLocale()
   {
      throw new NotImplementedYetException();
   }
}