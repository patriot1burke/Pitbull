package org.jboss.pitbull.crypto;


import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
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

   public static X509Certificate generateSelfSignedCertificate(KeyPair pair) throws Exception
   {
      /*
      X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);

      builder.addRDN(BCStyle.C, "US");
      builder.addRDN(BCStyle.O, "Red Hat");
      builder.addRDN(BCStyle.L, "Westford");
      builder.addRDN(BCStyle.ST, "Massachusetts");
      builder.addRDN(BCStyle.E, "bburke@redhat.com");


      //
      // extensions
      //

      //
      // create the certificate - version 3 - without extensions
      //
      ContentSigner sigGen = new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider("BC").build(pair.getPrivate());
      X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(builder.build(), BigInteger.valueOf(1),
              new Date(System.currentTimeMillis() - 50000),
              new Date(System.currentTimeMillis() + 500000000L),builder.build(), pair.getPublic());

      X509Certificate cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certGen.build(sigGen));
      */

      KeyPair KPair = pair;
      String domainName = "localhost";
      X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();
      int i = new SecureRandom().nextInt();
      if (i < 0) i *= -1;
      v3CertGen.setSerialNumber(BigInteger.valueOf(i));
      v3CertGen.setIssuerDN(new X509Principal("CN=" + domainName + ", OU=None, O=None L=None, C=None"));
      v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30));
      v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365 * 10)));
      v3CertGen.setSubjectDN(new X509Principal("CN=" + domainName + ", OU=None, O=None L=None, C=None"));
      v3CertGen.setPublicKey(KPair.getPublic());
      v3CertGen.setSignatureAlgorithm("MD5WithRSAEncryption");

      X509Certificate cert = v3CertGen.generateX509Certificate(KPair.getPrivate());
      return cert;
   }

   public static KeyStore generateKeyStore() throws Exception
   {
      KeyPair keyPair = KeyPairGenerator.getInstance("RSA", "BC").generateKeyPair();
      X509Certificate cert = KeyTools.generateSelfSignedCertificate(keyPair);

      KeyStore ks = KeyStore.getInstance("JKS");
      ks.load(null, null);
      Certificate[] certs = {cert};
      ks.setKeyEntry("alias", keyPair.getPrivate(), new char[]{'p', 'a', 's', 's', 'w', 'o', 'r', 'd'}, certs);
      return ks;

   }
}
