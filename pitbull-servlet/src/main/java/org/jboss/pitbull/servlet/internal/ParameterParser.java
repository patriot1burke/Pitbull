package org.jboss.pitbull.servlet.internal;

import org.jboss.pitbull.util.MultivalueMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ParameterParser
{
   public static Map<String, String[]> parseParameters(String queryString, InputStream is) throws IOException
   {
      MultivalueMap<String, String> params = new MultivalueMap<String, String>();
      parseQueryParameters(params, queryString);
      parseForm(params, is);
      Map<String, String[]> rtn = new HashMap<String, String[]>();
      for (Map.Entry<String, List<String>> entry : params.entrySet())
      {
         String[] list = entry.getValue().toArray(new String[entry.getValue().size()]);
         rtn.put(entry.getKey(), list);
      }
      return rtn;
   }

   public static void parseForm(MultivalueMap<String, String> paramMap, InputStream entityStream)
           throws IOException
   {
      char[] buffer = new char[100];
      StringBuffer buf = new StringBuffer();
      BufferedReader reader = new BufferedReader(new InputStreamReader(entityStream));

      int wasRead = 0;
      do
      {
         wasRead = reader.read(buffer, 0, 100);
         if (wasRead > 0) buf.append(buffer, 0, wasRead);
      } while (wasRead > -1);

      String form = buf.toString();


      String[] params = form.split("&");

      for (String param : params)
      {
         if (param.indexOf('=') >= 0)
         {
            String[] nv = param.split("=");
            String val = nv.length > 1 ? nv[1] : "";
            paramMap.add(URLDecoder.decode(nv[0], "UTF-8"), URLDecoder.decode(val, "UTF-8"));
         }
         else
         {
            paramMap.add(param, "");
         }
      }
   }

   public static void parseQueryParameters(MultivalueMap<String, String> paramMap, String queryString)
   {
      if (queryString == null || queryString.equals("")) return;

      String[] params = queryString.split("&");

      for (String param : params)
      {
         if (param.indexOf('=') >= 0)
         {
            String[] nv = param.split("=");
            try
            {
               String name = URLDecoder.decode(nv[0], "UTF-8");
               String val = nv.length > 1 ? nv[1] : "";
               paramMap.add(name, URLDecoder.decode(val, "UTF-8"));
            }
            catch (UnsupportedEncodingException e)
            {
               throw new RuntimeException(e);
            }
         }
         else
         {
            try
            {
               String name = URLDecoder.decode(param, "UTF-8");
               paramMap.add(name, "");
            }
            catch (UnsupportedEncodingException e)
            {
               throw new RuntimeException(e);
            }
         }
      }
   }

}
