package org.jboss.pitbull.spi;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ZeroCopyResponseHandler
{
   void setWriter(ZeroCopyResponseWriter writer);
}
