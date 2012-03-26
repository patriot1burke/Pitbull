package org.jboss.pitbull.crypto;


import org.bouncycastle.x509.X509V1CertificateGenerator;

import javax.security.auth.x500.X500Principal;
import java.security.cert.Certificate;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class KeyTools
{
   static
   {
      BouncyIntegration.init();
   }

   public static X509Certificate generateTestCertificate(KeyPair pair) throws InvalidKeyException,
           NoSuchProviderException, SignatureException
   {

      X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();

      certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
      certGen.setIssuerDN(new X500Principal("CN=Test Certificate"));
      certGen.setNotBefore(new Date(System.currentTimeMillis() - 10000));
      certGen.setNotAfter(new Date(System.currentTimeMillis() + 10000));
      certGen.setSubjectDN(new X500Principal("CN=Test Certificate"));
      certGen.setPublicKey(pair.getPublic());
      certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

      return certGen.generateX509Certificate(pair.getPrivate(), "BC");
   }

   public static KeyStore generateKeyStore() throws Exception
   {
      KeyPair keyPair = KeyPairGenerator.getInstance("RSA", "BC").generateKeyPair();
      PrivateKey privateKey = keyPair.getPrivate();
      X509Certificate cert = KeyTools.generateTestCertificate(keyPair);

      KeyStore ks = KeyStore.getInstance("JKS", "BC");
      ks.load(null, null);
      Certificate[] certs = {cert};
      ks.setKeyEntry("alias", privateKey.getEncoded(), certs);
      return ks;

   }
}
