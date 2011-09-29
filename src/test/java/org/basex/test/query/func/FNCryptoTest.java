package org.basex.test.query.func;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.basex.core.cmd.DropDB;
import org.basex.test.query.AdvancedQueryTest;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

/**
 * This class tests the functions of the EXPath Cryptographic module.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public class FNCryptoTest extends AdvancedQueryTest {

  /** Test database name. */
  private static final String DB = Util.name(FNCryptoTest.class);

  /**
   * Test method for crypto:encrypt and crypto:decrypt with symmetric keys.
   */
  @Test
  public void encryption1() {
    final String msg = "messagemessagemessagemessagemessagemessagemessage";

    //DES/CBC/PKCS5Padding
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "let $e := c:encrypt('" + msg + "','symmetric','aaabbbaa'," +
        "'DES') return c:decrypt($e,'symmetric'," +
        "'aaabbbaa','DES')", msg);
  }

  /**
   * Test method for crypto:encrypt and crypto:decrypt with symmetric keys.
   */
  @Test
  public void encryption2() {
    final String msg = "messagemessagemessagemessagemessagemessagemessage";

    //DES/CBC/PKCS5Padding
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "let $e := c:encrypt('" + msg + "','symmetric','abababababababab'," +
        "'AES') return c:decrypt($e,'symmetric'," +
        "'abababababababab','AES')", msg);
  }

//  /**
//   * Test method for crypto:encrypt and crypto:decrypt with asymmetric keys.
//   */
//  @Test
//  public void encryptionAsym1() {
//    final String msg = "messagemessagemessagemessagemessagemessagemessage";
//
//    PublicKey puk = null;
//    PrivateKey prk = null;
//    try {
//
//      final KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
//      final KeyPair kp = gen.generateKeyPair();
//      puk = kp.getPublic();
//      prk = kp.getPrivate();
//
//    } catch(NoSuchAlgorithmException e) {
//      e.printStackTrace();
//    }
//
//    query("declare namespace c = 'http://expath.org/ns/crypto';" +
//       "let $e := c:encrypt('" + msg + "','asymmetric','" + prk + "','RSA')" +
//        "return c:decrypt($e,'asymmetric','" + puk + "','RSA')", msg);
//  }

  @Test
  public void hmacMD5hex() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:hmac('message','key','hmacMd5', 'hex')",
        "\"4E4748E62B463521F6775FBF921234B5\"");
  }

  @Test
  public void hmacMD5base64() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:hmac('message','key','hmacMd5', 'base64')",
        "\"4E4748E62B463521F6775FBF921234B5\"");
  }

  @Test
  public void hmacSHA1hex() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:hmac('message','key','hmacsha1', 'hex')",
        "\"4E4748E62B463521F6775FBF921234B5\"");
  }

  @Test
  public void hmacSHA1base64() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:hmac('message','key','hmacsha1', 'base64')",
        "\"4E4748E62B463521F6775FBF921234B5\"");
  }

  @Test
  public void validateSignature1() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','','','',''))",
        "true");
  }

  @Test
  public void validateSignature1b() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(" +
        "c:generate-signature(" +
        "<a/>,'','SHA1','DSA_SHA1','','enveloped'))",
        "true");
  }

  @Test
  public void validateSignature1c() {
    String input = "<a><Signature xmlns='http://www.w3.org/2000/09/xmldsig#'>" +
        "<SignedInfo><CanonicalizationMethod " +
        "Algorithm='http://www.w3.org/TR/2001/REC-xml-c14n-20010315'/>" +
        "<SignatureMethod Algorithm='http://www.w3.org/2000/09/xmldsig#rsa-" +
        "sha1'/><Reference URI=''><Transforms><Transform Algorithm='http://" +
        "www.w3.org/2000/09/xmldsig#enveloped-signature'/></Transforms>" +
        "<DigestMethod Algorithm='http://www.w3.org/2000/09/xmldsig#sha1'/>" +
        "<DigestValue>9hvH4qztnIYgYfJDRLnEMPJdoaY=</DigestValue></Reference>" +
        "</SignedInfo><SignatureValue>W/BpXt9odK+Ot2cU0No0+tzwAJyqSx+CRMXG2B" +
        "T6NRc2qbMMSB7l+RcR6jwsu2Smt0LCltR1YFLTPoD+GCarZA==</SignatureValue>" +
        "<KeyInfo><KeyValue><RSAKeyValue><Modulus>mH+uHBX+3mE9bgWzcDym0pnyu" +
        "W3ca6EexNvQ/sAKgDNmO1xFNgVWSgKGMxmaGRzGyPi+8+KeGKGM0mS1jpRPQQ==" +
        "</Modulus><Exponent>AQAB</Exponent></RSAKeyValue></KeyValue>" +
        "</KeyInfo></Signature></a>";

    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(" + input + ")",
        "true");
  }

  @Test
  public void validateSignatureWithCanonicalization() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>," +
        "'exclusive','','','',''))",
        "true");
  }

  @Test
  public void validateSignatureWithCanonicalization2() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>," +
        "'exclusive-with-comments','','','',''))",
        "true");
  }

  @Test
  public void validateSignatureWithCanonicalization3() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>," +
        "'inclusive','','','',''))",
        "true");
  }

  @Test
  public void validateSignatureWithCanonicalization4() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>," +
        "'inclusive-with-comments','','','',''))",
        "true");
  }

  @Test
  public void validateSignatureWithDigestAlgorithm() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','SHA1','','',''))",
        "true");
  }

  @Test
  public void validateSignatureWithDigestAlgorithm2() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','SHA256','','',''))",
        "true");
  }

  @Test
  public void validateSignatureWithDigestAlgorithm3() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','SHA512','','',''))",
        "true");
  }

  /**
   * Tests encryption algorithm arguments.
   */
  @Test
  public void validateSignatureWithSignatureAlgorithm() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'',''," +
        "'DSA_SHA1','',''))",
        "true");
  }

  /**
   * Tests encryption algorithm arguments.
   */
  @Test
  public void validateSignatureWithSignatureAlgorithm2() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'',''," +
        "'RSA_SHA1','',''))",
        "true");
  }

  /**
   * Tests signing a node and adding a specific namespace prefix to the
   * signature element.
   */
  @Test
  public void validateSignatureWithSignatureNamespace3() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','','','prefix',''))",
        "true");
  }

  /**
   * Test an enveloped signature.
   */
  @Test
  public void validateSignatureWithSignatureType() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','','',''," +
        "'enveloped'))",
        "true");
  }

  /**
   * Tests an enveloping signature.
   */
  @Test
  public void validateSignatureWithSignatureType2() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','','',''," +
        "'enveloping'))",
        "true");
  }

  /**
   * Tests a detached signature.
   */
  @Test
  public void validateSignatureWithSignatureType3() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','','',''," +
        "'detached'))",
        "true");
  }

  /**
   * Tests calling generate-signature with a wrong type argument.
   */
  @Test/*(expected = IndexOutOfBoundsException.class)*/
  public void validateSignatureWithSignatureTypeFAIL() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','','',''," +
        "'xxx'))");
    Assert.fail();
  }

  @Test
  public void validateSignatureWithXPath() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a><n/><n/></a>," +
        "'','','',''," +
        "'','/a/n'))",
        "true");
  }

  @Test
  public void validateSignatureWithCertificate() {
    // Command to create java keystore
    // keytool -genkey -keyalg RSA -alias basexselfsigned -keystore
    // keystore.jks -storepass password -validity 360

    final String certificate =
        "<digital-certificate>" +
        "<keystore-type>JKS</keystore-type>" +
        "<keystore-password>password</keystore-password>" +
        "<key-alias>basexselfsigned</key-alias>" +
        "<private-key-password>password</private-key-password>" +
        "<keystore-uri>/Users/lukas/keystore.jks</keystore-uri>" +
        "</digital-certificate>";

    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','','','',''," +
        certificate + "))",
        "true");
  }

  @Test
  public void validateSignatureWithXPathAndCertificate() {
    final String certificate =
        "<digital-certificate>" +
        "<keystore-type>JKS</keystore-type>" +
        "<keystore-password>password</keystore-password>" +
        "<key-alias>basexselfsigned</key-alias>" +
        "<private-key-password>password</private-key-password>" +
        "<keystore-uri>/Users/lukas/keystore.jks</keystore-uri>" +
        "</digital-certificate>";

    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a><n/><n/></a>," +
        "'','','','','','/a/n'," +
        certificate + "))",
        "true");
  }

  @Test
  public void validateSignatureFullySpecified() {
    final String certificate =
        "<digital-certificate>" +
        "<keystore-type>JKS</keystore-type>" +
        "<keystore-password>password</keystore-password>" +
        "<key-alias>basexselfsigned</key-alias>" +
        "<private-key-password>password</private-key-password>" +
        "<keystore-uri>/Users/lukas/keystore.jks</keystore-uri>" +
        "</digital-certificate>";

    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a><n/></a>," +
        "'exclusive','SHA512','RSA_SHA1','myPrefix','enveloped','/a/n',"
        + certificate + "))",
        "true");
  }

  /**
   * Deletes the test db.
   * @throws Exception exception
   */
  @AfterClass
  public static void end() throws Exception {
    new DropDB(DB).execute(CONTEXT);
    CONTEXT.close();
  }
}