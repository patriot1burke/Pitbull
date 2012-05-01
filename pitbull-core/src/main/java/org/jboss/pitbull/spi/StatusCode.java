package org.jboss.pitbull.spi;

/**
 * Response status code representation.  Works as much as possible like an enum so that you can perform simple address
 * equality operation (==).  There is no public constructor, so any new codes must be created calling the create() method
 * which has specific semantics.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class StatusCode
{
   /**
    * 100 Continue, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode CONTINUE = new StatusCode(100, "Continue");

   /**
    * 101 Switching Protocols, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode SWITCHING_PROTOCOLS = new StatusCode(101, "Switching Protocols");

   /**
    * 102 Processing (WebDAV, RFC2518)
    */
   public static final StatusCode PROCESSING = new StatusCode(102, "Processing");

   /**
    * 200 OK, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode OK = new StatusCode(200, "OK");

   /**
    * 201 Created, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode CREATED = new StatusCode(201, "Created");

   /**
    * 202 Accepted, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode ACCEPTED = new StatusCode(202, "Accepted");

   /**
    * 203 Non-Authoritative Information (since HTTP/1.1), see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode NON_AUTHORITATIVE_INFORMATION = new StatusCode(203, "Non-Authoritative Information");

   /**
    * 204 No Content, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode NO_CONTENT = new StatusCode(204, "No Content");

   /**
    * 205 Reset Content, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode RESET_CONTENT = new StatusCode(205, "Reset Content");

   /**
    * 206 Partial Content, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode PARTIAL_CONTENT = new StatusCode(206, "Partial Content");

   /**
    * 207 Multi-Status (WebDAV, RFC2518)
    */
   public static final StatusCode MULTI_STATUS = new StatusCode(207, "Multi-Status");

   /**
    * 300 Multiple Choices, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode MULTIPLE_CHOICES = new StatusCode(300, "Multiple Choices");

   /**
    * 301 Moved Permanently, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode MOVED_PERMANENTLY = new StatusCode(301, "Moved Permanently");

   /**
    * 302 Found, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode FOUND = new StatusCode(302, "Found");

   /**
    * 303 See Other (since HTTP/1.1), see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode SEE_OTHER = new StatusCode(303, "See Other");

   /**
    * 304 Not Modified, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode NOT_MODIFIED = new StatusCode(304, "Not Modified");

   /**
    * 305 Use Proxy (since HTTP/1.1), see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode USE_PROXY = new StatusCode(305, "Use Proxy");

   /**
    * 307 Temporary Redirect (since HTTP/1.1), see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode TEMPORARY_REDIRECT = new StatusCode(307, "Temporary Redirect");

   /**
    * 400 Bad Request, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode BAD_REQUEST = new StatusCode(400, "Bad Request");

   /**
    * 401 Unauthorized, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode UNAUTHORIZED = new StatusCode(401, "Unauthorized");

   /**
    * 402 Payment Required, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode PAYMENT_REQUIRED = new StatusCode(402, "Payment Required");

   /**
    * 403 Forbidden, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode FORBIDDEN = new StatusCode(403, "Forbidden");

   /**
    * 404 Not Found, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode NOT_FOUND = new StatusCode(404, "Not Found");

   /**
    * 405 Method Not Allowed, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode METHOD_NOT_ALLOWED = new StatusCode(405, "Method Not Allowed");

   /**
    * 406 Not Acceptable, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode NOT_ACCEPTABLE = new StatusCode(406, "Not Acceptable");

   /**
    * 407 Proxy Authentication Required, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode PROXY_AUTHENTICATION_REQUIRED = new StatusCode(407, "Proxy Authentication Required");

   /**
    * 408 Request Timeout, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode REQUEST_TIMEOUT = new StatusCode(408, "Request Timeout");

   /**
    * 409 Conflict, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode CONFLICT = new StatusCode(409, "Conflict");

   /**
    * 410 Gone, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode GONE = new StatusCode(410, "Gone");

   /**
    * 411 Length Required, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode LENGTH_REQUIRED = new StatusCode(411, "Length Required");

   /**
    * 412 Precondition Failed, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode PRECONDITION_FAILED = new StatusCode(412, "Precondition Failed");

   /**
    * 413 Request Entity Too Large, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode REQUEST_ENTITY_TOO_LARGE = new StatusCode(413, "Request Entity Too Large");

   /**
    * 414 Request-URI Too Long, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode REQUEST_URI_TOO_LONG = new StatusCode(414, "Request-URI Too Long");

   /**
    * 415 Unsupported Media Type, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode UNSUPPORTED_MEDIA_TYPE = new StatusCode(415, "Unsupported Media Type");

   /**
    * 416 Requested Range Not Satisfiable, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode REQUESTED_RANGE_NOT_SATISFIABLE = new StatusCode(416, "Requested Range Not Satisfiable");

   /**
    * 417 Expectation Failed, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode EXPECTATION_FAILED = new StatusCode(417, "Expectation Failed");

   /**
    * 422 Unprocessable Entity (WebDAV, RFC4918)
    */
   public static final StatusCode UNPROCESSABLE_ENTITY = new StatusCode(422, "Unprocessable Entity");

   /**
    * 423 Locked (WebDAV, RFC4918)
    */
   public static final StatusCode LOCKED = new StatusCode(423, "Locked");

   /**
    * 424 Failed Dependency (WebDAV, RFC4918)
    */
   public static final StatusCode FAILED_DEPENDENCY = new StatusCode(424, "Failed Dependency");

   /**
    * 425 Unordered Collection (WebDAV, RFC3648)
    */
   public static final StatusCode UNORDERED_COLLECTION = new StatusCode(425, "Unordered Collection");

   /**
    * 426 Upgrade Required (RFC2817)
    */
   public static final StatusCode UPGRADE_REQUIRED = new StatusCode(426, "Upgrade Required");

   /**
    * 500 Internal Server Error, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode INTERNAL_SERVER_ERROR = new StatusCode(500, "Internal Server Error");

   /**
    * 501 Not Implemented, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode NOT_IMPLEMENTED = new StatusCode(501, "Not Implemented");

   /**
    * 502 Bad Gateway, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode BAD_GATEWAY = new StatusCode(502, "Bad Gateway");

   /**
    * 503 Service Unavailable, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode SERVICE_UNAVAILABLE = new StatusCode(503, "Service Unavailable");

   /**
    * 504 Gateway Timeout, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode GATEWAY_TIMEOUT = new StatusCode(504, "Gateway Timeout");

   /**
    * 505 HTTP Version Not Supported, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1 code definitions</a>}.
    */
   public static final StatusCode HTTP_VERSION_NOT_SUPPORTED = new StatusCode(505, "HTTP Version Not Supported");

   /**
    * 506 Variant Also Negotiates (RFC2295)
    */
   public static final StatusCode VARIANT_ALSO_NEGOTIATES = new StatusCode(506, "Variant Also Negotiates");

   /**
    * 507 Insufficient Storage (WebDAV, RFC4918)
    */
   public static final StatusCode INSUFFICIENT_STORAGE = new StatusCode(507, "Insufficient Storage");

   /**
    * 510 Not Extended (RFC2774)
    */
   public static final StatusCode NOT_EXTENDED = new StatusCode(510, "Not Extended");

   private final int code;
   private final String statusMessage;

   StatusCode(int code, String statusMessage)
   {
      this.code = code;
      this.statusMessage = statusMessage;
   }

   public int getCode()
   {
      return code;
   }

   public String getStatusMessage()
   {
      return statusMessage;
   }

   /**
    * Creates a new StatusCode.  The message parameter will be ignored if the status code matches one of the
    * predefined constants in this class and that predefined constant instance will be returned instead.
    *
    * @param statusCode
    * @param message
    * @return
    */
   public static StatusCode create(final int statusCode, final String message)
   {
      StatusCode found = valueOf(statusCode);
      if (found != null) return found;
      return new StatusCode(statusCode, message);
   }

   /**
    * Convert a integer status code into a corresponding Status constant value
    *
    * @param code
    * @return the matched Status or null if no match of constants defined in this class
    */
   public static StatusCode valueOf(final int statusCode)
   {
      switch (statusCode)
      {
         case 100:
            return CONTINUE;
         case 101:
            return SWITCHING_PROTOCOLS;
         case 102:
            return PROCESSING;
         case 200:
            return OK;
         case 201:
            return CREATED;
         case 202:
            return ACCEPTED;
         case 203:
            return NON_AUTHORITATIVE_INFORMATION;
         case 204:
            return NO_CONTENT;
         case 205:
            return RESET_CONTENT;
         case 206:
            return PARTIAL_CONTENT;
         case 207:
            return MULTI_STATUS;
         case 300:
            return MULTIPLE_CHOICES;
         case 301:
            return MOVED_PERMANENTLY;
         case 302:
            return FOUND;
         case 303:
            return SEE_OTHER;
         case 304:
            return NOT_MODIFIED;
         case 305:
            return USE_PROXY;
         case 307:
            return TEMPORARY_REDIRECT;
         case 400:
            return BAD_REQUEST;
         case 401:
            return UNAUTHORIZED;
         case 402:
            return PAYMENT_REQUIRED;
         case 403:
            return FORBIDDEN;
         case 404:
            return NOT_FOUND;
         case 405:
            return METHOD_NOT_ALLOWED;
         case 406:
            return NOT_ACCEPTABLE;
         case 407:
            return PROXY_AUTHENTICATION_REQUIRED;
         case 408:
            return REQUEST_TIMEOUT;
         case 409:
            return CONFLICT;
         case 410:
            return GONE;
         case 411:
            return LENGTH_REQUIRED;
         case 412:
            return PRECONDITION_FAILED;
         case 413:
            return REQUEST_ENTITY_TOO_LARGE;
         case 414:
            return REQUEST_URI_TOO_LONG;
         case 415:
            return UNSUPPORTED_MEDIA_TYPE;
         case 416:
            return REQUESTED_RANGE_NOT_SATISFIABLE;
         case 417:
            return EXPECTATION_FAILED;
         case 422:
            return UNPROCESSABLE_ENTITY;
         case 423:
            return LOCKED;
         case 424:
            return FAILED_DEPENDENCY;
         case 425:
            return UNORDERED_COLLECTION;
         case 426:
            return UPGRADE_REQUIRED;
         case 500:
            return INTERNAL_SERVER_ERROR;
         case 501:
            return NOT_IMPLEMENTED;
         case 502:
            return BAD_GATEWAY;
         case 503:
            return SERVICE_UNAVAILABLE;
         case 504:
            return GATEWAY_TIMEOUT;
         case 505:
            return HTTP_VERSION_NOT_SUPPORTED;
         case 506:
            return VARIANT_ALSO_NEGOTIATES;
         case 507:
            return INSUFFICIENT_STORAGE;
         case 510:
            return NOT_EXTENDED;
         default:
            return null;
      }

   }

   @Override
   public int hashCode()
   {
      return code;
   }

   @Override
   public boolean equals(Object o)
   {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      StatusCode that = (StatusCode) o;

      if (code != that.code) return false;

      return true;
   }

   @Override
   public String toString()
   {
      StringBuilder buf = new StringBuilder(statusMessage.length() + 5);
      buf.append(code);
      buf.append(' ');
      buf.append(statusMessage);
      return buf.toString();
   }
}
