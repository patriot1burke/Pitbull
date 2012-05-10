package org.jboss.pitbull.websocket;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public enum WebSocketVersion
{
   HYBI_00("0"),
   HYBI_07("7"),
   HYBI_08("8"),
   HYBI_13("13");

   private final String code;

   WebSocketVersion(String code)
   {
      this.code = code;
   }

   public String getCode()
   {
      return code;
   }
}
