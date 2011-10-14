package org.jboss.pitbull.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ContentType
{

   private String type;
   private String subtype;
   private Map<String, String> parameters;

   private static final Map<String, String> empty = Collections.emptyMap();

   public static final String WILDCARD = "*";

   public static String getCharsetFromContentType(String contentType)
   {

      if (contentType == null)
         return (null);
      int start = contentType.indexOf("charset=");
      if (start < 0)
         return (null);
      String encoding = contentType.substring(start + 8);
      int end = encoding.indexOf(';');
      if (end >= 0)
         encoding = encoding.substring(0, end);
      encoding = encoding.trim();
      if ((encoding.length() > 2) && (encoding.startsWith("\""))
              && (encoding.endsWith("\"")))
         encoding = encoding.substring(1, encoding.length() - 1);
      return (encoding.trim());
   }

   public static ContentType valueOf(String type) throws IllegalArgumentException
   {
      String params = null;
      int idx = type.indexOf(";");
      if (idx > -1)
      {
         params = type.substring(idx + 1).trim();
         type = type.substring(0, idx);
      }
      String major = null;
      String subtype = null;
      String[] paths = type.split("/");
      if (paths.length < 2 && type.equals("*"))
      {
         major = "*";
         subtype = "*";

      }
      else if (paths.length != 2)
      {
         throw new IllegalArgumentException("Failure parsing ContentType string: " + type);
      }
      else if (paths.length == 2)
      {
         major = paths[0];
         subtype = paths[1];
      }
      if (params != null && !params.equals(""))
      {
         HashMap<String, String> typeParams = new HashMap<String, String>();

         int start = 0;

         while (start < params.length())
         {
            start = HeaderParameterParser.setParam(typeParams, params, start);
         }
         return new ContentType(major, subtype, typeParams);
      }
      else
      {
         return new ContentType(major, subtype);
      }
   }

   public ContentType(String type, String subtype, Map<String, String> parameters)
   {
      this.type = type == null ? WILDCARD : type;
      this.subtype = subtype == null ? WILDCARD : subtype;
      if (parameters == null)
      {
         this.parameters = empty;
      }
      else
      {
         Map<String, String> map = new TreeMap<String, String>(new Comparator<String>()
         {
            public int compare(String o1, String o2)
            {
               return o1.compareToIgnoreCase(o2);
            }
         });
         for (Map.Entry<String, String> e : parameters.entrySet())
         {
            map.put(e.getKey().toLowerCase(), e.getValue());
         }
         this.parameters = Collections.unmodifiableMap(map);
      }
   }

   public ContentType(String type, String subtype)
   {
      this(type, subtype, empty);
   }

   public ContentType()
   {
      this(WILDCARD, WILDCARD);
   }

   /**
    * Getter for primary type.
    *
    * @return value of primary type.
    */
   public String getType()
   {
      return this.type;
   }

   public boolean isWildcardType()
   {
      return this.getType().equals(WILDCARD);
   }

   public String getSubtype()
   {
      return this.subtype;
   }

   public boolean isWildcardSubtype()
   {
      return this.getSubtype().equals(WILDCARD);
   }

   public Map<String, String> getParameters()
   {
      return parameters;
   }

   public boolean isCompatible(ContentType other)
   {
      if (other == null)
         return false;
      if (type.equals(WILDCARD) || other.type.equals(WILDCARD))
         return true;
      else if (type.equalsIgnoreCase(other.type) && (subtype.equals(WILDCARD) || other.subtype.equals(WILDCARD)))
         return true;
      else
         return this.type.equalsIgnoreCase(other.type)
                 && this.subtype.equalsIgnoreCase(other.subtype);
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == null)
         return false;
      if (!(obj instanceof ContentType))
         return false;
      ContentType other = (ContentType) obj;
      return (this.type.equalsIgnoreCase(other.type)
              && this.subtype.equalsIgnoreCase(other.subtype)
              && this.parameters.equals(other.parameters));
   }

   @Override
   public int hashCode()
   {
      return (this.type.toLowerCase() + this.subtype.toLowerCase()).hashCode() + this.parameters.hashCode();
   }

   /**
    * Convert the media type to a string suitable for use as the value of a
    * corresponding HTTP header.
    *
    * @return a stringified media type
    */
   @Override
   public String toString()
   {
      ContentType type = this;
      String rtn = type.getType().toLowerCase() + "/" + type.getSubtype().toLowerCase();
      if (type.getParameters() == null || type.getParameters().size() == 0) return rtn;
      for (String name : type.getParameters().keySet())
      {
         String val = type.getParameters().get(name);
         rtn += ";" + name + "=\"" + val + "\"";
      }
      return rtn;
   }
}
