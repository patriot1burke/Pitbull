package org.jboss.pitbull;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mutable headers representation.  Can obtain a list of headers as they were added.  Maintains sequence of added
 * headers.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface OrderedHeaders
{
   /**
    * Immutable list of header entries
    *
    * @return
    */
   List<Map.Entry<String, String>> getHeaderList();

   /**
    * Immutable set of header names
    *
    * @return
    */
   Set<String> getHeaderNames();

   /**
    * Immutable list of header values
    *
    * @param name
    * @return
    */
   List<String> getHeaderValues(String name);

   String getFirstHeader(String name);

   void addHeader(String name, String value);

   /**
    * Removes old header values before setting this new one
    *
    * @param name
    * @param value
    */
   void setHeader(String name, String value);

   void removeHeader(String name);

   boolean containsHeader(String name);

   void clear();
}
