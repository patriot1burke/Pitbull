package org.jboss.pitbull.servlet;

import org.jboss.pitbull.NotImplementedYetException;
import org.jboss.pitbull.spi.Connection;
import org.jboss.pitbull.spi.RequestHeader;
import org.jboss.pitbull.util.CaseInsensitiveMap;
import org.jboss.pitbull.util.ContentType;
import org.jboss.pitbull.util.DateUtil;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HttpServletRequestImpl implements HttpServletRequest
{

   protected Connection connection;
   protected InputStream underlyingInputStream;
   protected String authType;
   protected RequestHeader headerBlob;

   protected Map<String, Object> attributes = new HashMap<String, Object>();
   protected CaseInsensitiveMap<String> headers;
   protected Cookie[] cookies;
   protected ServletInputStream servletInputStream;
   protected BufferedReader reader;
   protected Date dateHeader;
   protected String contentType;
   protected String characterEncoding;
   protected String requestURL;
   protected String requestURI;
   protected String queryString;
   protected Map<String, String[]> parameters;
   protected DeploymentServletContext context;
   public static final String DEFAULT_CHARACTER_ENCODING = "ISO-8859-1";
   private static final Cookie[] emptyCookies = new Cookie[0];


   public void setContext(DeploymentServletContext context)
   {
      this.context = context;
   }

   public void setConnection(Connection connection)
   {
      this.connection = connection;
   }

   public void setIs(InputStream is)
   {
      this.underlyingInputStream = is;
   }

   public void setHeaderBlob(RequestHeader headerBlob)
   {
      this.headerBlob = headerBlob;
   }

   protected CaseInsensitiveMap<String> getHeaders()
   {
      if (headers == null)
      {
         headers = headerBlob.getHeaders();
      }
      return headers;
   }

   @Override
   public String getAuthType()
   {
      return authType;
   }

   @Override
   public Cookie[] getCookies()
   {
      if (cookies != null) return cookies;
      List<String> cookieHeaders = getHeaders().get("Cookie");
      if (cookieHeaders == null)
      {
         cookies = emptyCookies;
         return cookies;
      }

      List<Cookie> cookieList = new ArrayList<Cookie>();
      for (String cookieHeader : cookieHeaders)
      {
         Set<Cookie> cooks = CookieDecoder.decode(cookieHeader);
         cookieList.addAll(cooks);

      }
      cookies = cookieList.toArray(new Cookie[cookieList.size()]);
      return cookies;
   }

   @Override
   public long getDateHeader(String name)
   {
      if (dateHeader == null)
      {
         String val = getHeaders().getFirst("Date");
         if (val != null)
         {
            dateHeader = DateUtil.parseDate(val);
         }
      }
      if (dateHeader == null) return -1;
      else return dateHeader.getTime();
   }

   @Override
   public String getHeader(String name)
   {
      return getHeaders().getFirst(name);
   }

   @Override
   public Enumeration<String> getHeaders(String name)
   {
      final Iterator<String> it = getHeaders().get(name).iterator();
      return new Enumeration<String>()
      {
         @Override
         public boolean hasMoreElements()
         {
            return it.hasNext();
         }

         @Override
         public String nextElement()
         {
            return it.next();
         }
      };
   }

   @Override
   public Enumeration<String> getHeaderNames()
   {
      final Iterator<String> it = getHeaders().keySet().iterator();
      return new Enumeration<String>()
      {
         @Override
         public boolean hasMoreElements()
         {
            return it.hasNext();
         }

         @Override
         public String nextElement()
         {
            return it.next();
         }
      };
   }

   @Override
   public int getIntHeader(String name)
   {
      String val = getHeaders().getFirst(name);
      if (val == null) return -1;
      return Integer.parseInt(val);
   }

   @Override
   public String getMethod()
   {
      return headerBlob.getMethod();
   }

   @Override
   public String getPathInfo()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public String getPathTranslated()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public String getContextPath()
   {
      return context.getContextPath();
   }

   @Override
   public String getQueryString()
   {
      if (queryString != null) return queryString;
      String uri = headerBlob.getUri();
      int idx = uri.indexOf('?');
      if (idx >= 0)
      {
         queryString = uri.substring(idx + 1);
      }
      return queryString;
   }

   @Override
   public String getRemoteUser()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public boolean isUserInRole(String role)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public Principal getUserPrincipal()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public String getRequestedSessionId()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public String getRequestURI()
   {
      if (requestURI != null) return requestURI;
      String uri = headerBlob.getUri();
      int idx = uri.indexOf('?');
      if (idx >= 0)
      {
         uri = uri.substring(0, idx);
      }
      requestURI = uri;
      return requestURI;
   }

   @Override
   public StringBuffer getRequestURL()
   {
      // todo support for RequestDispatcher
      if (requestURL != null) return new StringBuffer(requestURL);
      StringBuilder builder = new StringBuilder();
      if (connection.isSecure()) builder.append("https://");
      else builder.append("http://");
      String host = getHeaders().getFirst("Host");
      if (host == null)
      {
         host = connection.getLocalAddress().getHostName();
      }
      builder.append(host);
      if (host.indexOf(':') < 0)
      {
         int local = connection.getLocalAddress().getPort();
         if (connection.isSecure() && local != 443)
         {
            builder.append(":").append(local);
         }
         else if (!connection.isSecure() && local != 80)
         {
            builder.append(":").append(local);
         }
      }
      builder.append(getRequestURI());
      requestURL = builder.toString();
      return new StringBuffer(requestURL);
   }

   @Override
   public String getServletPath()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public HttpSession getSession(boolean create)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public HttpSession getSession()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public boolean isRequestedSessionIdValid()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public boolean isRequestedSessionIdFromCookie()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public boolean isRequestedSessionIdFromURL()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public boolean isRequestedSessionIdFromUrl()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public boolean authenticate(HttpServletResponse response) throws IOException, ServletException
   {
      throw new NotImplementedYetException();
   }

   @Override
   public void login(String username, String password) throws ServletException
   {
      throw new NotImplementedYetException();
   }

   @Override
   public void logout() throws ServletException
   {
      throw new NotImplementedYetException();
   }

   @Override
   public Collection<Part> getParts() throws IOException, ServletException
   {
      throw new NotImplementedYetException();
   }

   @Override
   public Part getPart(String name) throws IOException, ServletException
   {
      throw new NotImplementedYetException();
   }

   @Override
   public Object getAttribute(String name)
   {
      return attributes.get(name);
   }

   @Override
   public Enumeration<String> getAttributeNames()
   {
      final Iterator<String> it = attributes.keySet().iterator();
      return new Enumeration<String>()
      {
         @Override
         public boolean hasMoreElements()
         {
            return it.hasNext();
         }

         @Override
         public String nextElement()
         {
            return it.next();
         }
      };
   }

   protected String getCharacterEncodingOrDefault()
   {
      String enc = getCharacterEncoding();
      if (enc == null) enc = DEFAULT_CHARACTER_ENCODING;
      return enc;
   }

   @Override
   public String getCharacterEncoding()
   {
      if (characterEncoding == null)
      {
         characterEncoding = ContentType.getCharsetFromContentType(getContentType());
      }
      return characterEncoding;
   }

   @Override
   public void setCharacterEncoding(String env) throws UnsupportedEncodingException
   {
      characterEncoding = env;
   }

   @Override
   public int getContentLength()
   {
      String len = getHeader("Content-Length");
      if (len == null) return -1;
      return Integer.parseInt(len);
   }

   @Override
   public String getContentType()
   {
      if (contentType == null)
      {
         contentType = getHeader("Content-Type");
      }
      return contentType;
   }

   @Override
   public ServletInputStream getInputStream() throws IOException
   {
      if (reader != null) throw new IllegalStateException("BufferedReader already being used by HttpServletRequest");
      if (servletInputStream == null)
      {
         servletInputStream = new ServletInputStreamImpl(underlyingInputStream);
      }
      return servletInputStream;
   }

   @Override
   public String getParameter(String name)
   {
      String[] values = getParameterMap().get(name);
      if (values == null || values.length == 0) return null;
      return values[0];
   }

   @Override
   public Enumeration<String> getParameterNames()
   {
      final Iterator<String> it = getParameterMap().keySet().iterator();
      return new Enumeration<String>()
      {
         @Override
         public boolean hasMoreElements()
         {
            return it.hasNext();
         }

         @Override
         public String nextElement()
         {
            return it.next();
         }
      };
   }

   @Override
   public String[] getParameterValues(String name)
   {
      return getParameterMap().get(name);
   }

   @Override
   public Map<String, String[]> getParameterMap()
   {
      if (parameters == null)
      {
         try
         {
            parameters = ParameterParser.parseParameters(getQueryString(), underlyingInputStream);
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
      }
      return parameters;
   }

   @Override
   public String getProtocol()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public String getScheme()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public String getServerName()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public int getServerPort()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public BufferedReader getReader() throws IOException
   {
      if (servletInputStream != null)
         throw new IllegalStateException("ServletInputStream already being used by HttpServletRequest");
      if (reader == null)
      {
         reader = new BufferedReader(new InputStreamReader(underlyingInputStream, getCharacterEncodingOrDefault()));
      }
      return reader;
   }

   @Override
   public String getRemoteAddr()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public String getRemoteHost()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public void setAttribute(String name, Object o)
   {
      attributes.put(name, o);
   }

   @Override
   public void removeAttribute(String name)
   {
      attributes.remove(name);
   }

   @Override
   public Locale getLocale()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public Enumeration<Locale> getLocales()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public boolean isSecure()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public RequestDispatcher getRequestDispatcher(String path)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public String getRealPath(String path)
   {
      throw new NotImplementedYetException();
   }

   @Override
   public int getRemotePort()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public String getLocalName()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public String getLocalAddr()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public int getLocalPort()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public ServletContext getServletContext()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public AsyncContext startAsync() throws IllegalStateException
   {
      throw new NotImplementedYetException();
   }

   @Override
   public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException
   {
      throw new NotImplementedYetException();
   }

   @Override
   public boolean isAsyncStarted()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public boolean isAsyncSupported()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public AsyncContext getAsyncContext()
   {
      throw new NotImplementedYetException();
   }

   @Override
   public DispatcherType getDispatcherType()
   {
      throw new NotImplementedYetException();
   }
}
