package org.jboss.pitbull.util;

import org.jboss.pitbull.spi.OrderedHeaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Header order is remembered.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class OrderedHeadersImpl implements OrderedHeaders
{
   protected List<Map.Entry<String, String>> headerList = new ArrayList<Map.Entry<String, String>>();
   protected CaseInsensitiveMap<Map.Entry<String, String>> headerMap = new CaseInsensitiveMap<Map.Entry<String, String>>();

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
   public boolean containsHeader(String name)
   {
      return headerMap.containsKey(name);
   }

   @Override
   public void clear()
   {
      headerMap.clear();
      headerList.clear();
   }


   @Override
   public List<Map.Entry<String, String>> getHeaderList()
   {
      return headerList;
   }

   @Override
   public Set<String> getHeaderNames()
   {
      return headerMap.keySet();
   }

   @Override
   public List<String> getHeaderValues(String name)
   {
      List<String> values = new ArrayList<String>();
      List<Map.Entry<String, String>> entries = headerMap.get(name);
      if (entries == null) return values;
      for (Map.Entry<String, String> entry : entries)
      {
         values.add(entry.getValue());
      }

      return values;
   }

   @Override
   public String getFirstHeader(String name)
   {
      Map.Entry<String, String> entry = headerMap.getFirst(name);
      if (entry == null) return null;
      return entry.getValue();
   }

   @Override
   public void addHeader(String name, String value)
   {
      Map.Entry<String, String> entry = new HeaderEntry(name, value);
      headerList.add(entry);
      headerMap.add(name, entry);
   }

   @Override
   public void setHeader(String name, String value)
   {
      removeHeader(name);
      if (value != null) addHeader(name, value);
   }

   @Override
   public void removeHeader(String name)
   {
      List<Map.Entry<String, String>> values = headerMap.remove(name);
      if (values != null)
      {
         for (Map.Entry<String, String> entry : values)
         {
            headerList.remove(entry);
         }
      }
   }


}
