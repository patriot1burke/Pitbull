package org.jboss.pitbull.internal.nio.http;

import org.jboss.pitbull.OrderedHeaders;

import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class HttpMessageDecoder
{
   //space ' '
   protected static final byte SP = 32;
   //tab ' '
   protected static final byte HT = 9;
   /**
    * Carriage return
    */
   protected static final byte CR = 13;
   /**
    * Equals '='
    */
   protected static final byte EQUALS = 61;
   /**
    * Line feed character
    */
   protected static final byte LF = 10;
   /**
    * carriage return line feed
    */
   protected static final byte[] CRLF = new byte[]{CR, LF};
   /**
    * Colon ':'
    */
   protected static final byte COLON = 58;
   /**
    * Semicolon ';'
    */
   protected static final byte SEMICOLON = 59;
   /**
    * comma ','
    */
   protected static final byte COMMA = 44;
   protected static final byte DOUBLE_QUOTE = '"';
   protected StringBuilder currentString = new StringBuilder(64);
   protected String currentHeaderName;
   protected String currentHeaderValue;
   protected States currentState = States.SKIP_CONTROL_CHARS;


   protected interface State
   {
      boolean process(HttpMessageDecoder decoder, ByteBuffer buffer);
   }

   protected enum States implements State
   {
      SKIP_CONTROL_CHARS
              {
                 @Override
                 public boolean process(HttpMessageDecoder decoder, ByteBuffer buffer)
                 {
                    return decoder.skipControlChars(buffer);
                 }
              },

      READ_INITIAL
              {
                 @Override
                 public boolean process(HttpMessageDecoder decoder, ByteBuffer buffer)
                 {
                    return decoder.readInitial(buffer);
                 }
              },

      READ_HEADERS
              {
                 @Override
                 public boolean process(HttpMessageDecoder decoder, ByteBuffer buffer)
                 {
                    return decoder.readHeader(buffer);
                 }
              },

      DONE
              {
                 @Override
                 public boolean process(HttpMessageDecoder decoder, ByteBuffer buffer)
                 {
                    return false;
                 }
              }
   }

   /**
    * @param buffer must be flipped
    */
   public boolean process(ByteBuffer buffer)
   {
      if (currentState == States.DONE) return true;
      while (currentState.process(this, buffer)) ;
      if (currentState == States.DONE) return true;
      return false;
   }


   protected abstract OrderedHeaders getHeaders();

   protected boolean readHeader(ByteBuffer buffer)
   {
      for (; ; )
      {
         String line = readLine(buffer);
         if (line == null) return false;

         if (line.length() == 0)
         {
            if (currentHeaderName != null && currentHeaderValue != null)
            {
               getHeaders().addHeader(currentHeaderName, currentHeaderValue);
            }
            currentState = States.DONE;
            return false;
         }
         else
         {
            char first = line.charAt(0);
            if (currentHeaderName != null && (first == ' ' || first == '\t'))
            {
               currentHeaderValue += ' ' + line.trim();
            }
            else
            {
               if (currentHeaderName != null)
               {
                  getHeaders().addHeader(currentHeaderName, currentHeaderValue);
                  currentHeaderName = null;
                  currentHeaderValue = null;
               }
               String[] split = splitHeader(line);
               currentHeaderName = split[0];
               currentHeaderValue = split[1];
            }
         }
      }

   }

   protected abstract boolean readInitial(ByteBuffer buffer);

   protected String readLine(ByteBuffer buffer)
   {
      String line = null;
      while (buffer.hasRemaining())
      {
         byte b = buffer.get();
         if (b == LF)
         {
            // strip out \r
            if (currentString.length() > 0 && currentString.charAt(currentString.length() - 1) == '\r')
               currentString.setLength(currentString.length() - 1);
            line = currentString.toString();
            currentString = new StringBuilder();
            break;
         }
         else
         {
            currentString.append((char) b);
         }
      }
      return line;
   }

   protected boolean skipControlChars(ByteBuffer buffer)
   {
      while (buffer.hasRemaining())
      {
         char c = (char) buffer.get();
         if (!Character.isISOControl(c) &&
                 !Character.isWhitespace(c))
         {
            buffer.position(buffer.position() - 1);
            currentState = States.READ_INITIAL;
            return true;
         }
      }
      return false;
   }

   protected String[] splitHeader(String sb)
   {
      final int length = sb.length();
      int nameStart;
      int nameEnd;
      int colonEnd;
      int valueStart;
      int valueEnd;

      nameStart = findNonWhitespace(sb, 0);
      for (nameEnd = nameStart; nameEnd < length; nameEnd++)
      {
         char ch = sb.charAt(nameEnd);
         if (ch == ':' || Character.isWhitespace(ch))
         {
            break;
         }
      }

      for (colonEnd = nameEnd; colonEnd < length; colonEnd++)
      {
         if (sb.charAt(colonEnd) == ':')
         {
            colonEnd++;
            break;
         }
      }

      valueStart = findNonWhitespace(sb, colonEnd);
      if (valueStart == length)
      {
         return new String[]{
                 sb.substring(nameStart, nameEnd),
                 ""
         };
      }

      valueEnd = findEndOfString(sb);
      return new String[]{
              sb.substring(nameStart, nameEnd),
              sb.substring(valueStart, valueEnd)
      };
   }

   protected int findNonWhitespace(String sb, int offset)
   {
      int result;
      for (result = offset; result < sb.length(); result++)
      {
         if (!Character.isWhitespace(sb.charAt(result)))
         {
            break;
         }
      }
      return result;
   }

   protected int findWhitespace(String sb, int offset)
   {
      int result;
      for (result = offset; result < sb.length(); result++)
      {
         if (Character.isWhitespace(sb.charAt(result)))
         {
            break;
         }
      }
      return result;
   }

   protected int findEndOfString(String sb)
   {
      int result;
      for (result = sb.length(); result > 0; result--)
      {
         if (!Character.isWhitespace(sb.charAt(result - 1)))
         {
            break;
         }
      }
      return result;
   }

}
