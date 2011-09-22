package org.basex.test.query.func;

import org.basex.core.cmd.CreateDB;
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
  public void encryptionSym1() {
    final String msg = "messagemessagemessagemessagemessagemessagemessage";

    //DES/CBC/PKCS5Padding
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "let $e := c:encrypt('" + msg + "','symmetric','aaabbbaa','DES/CBC/PKCS5Padding')" +
        "return c:decrypt($e,'symmetric','aaabbbaa','DES/CBC/PKCS5Padding')", msg);
  }

  /**
   * Test method for crypto:encrypt and crypto:decrypt with symmetric keys.
   */
  @Test
  public void encryptionSym2() {
    final String msg = "messagemessagemessagemessagemessagemessagemessage";

    //DES/CBC/PKCS5Padding
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "let $e := c:encrypt('" + msg + "','symmetric','abababababababab','AES/CBC/PKCS5Padding')" +
        "return c:decrypt($e,'symmetric','abababababababab','AES/CBC/PKCS5Padding')", msg);
  }

  /**
   * Test method for crypto:encrypt and crypto:decrypt with asymmetric keys.
   */
  @Test
  public void encryptionAsym1() {
    final String msg = "messagemessagemessagemessagemessagemessagemessage";

    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "let $e := c:encrypt('" + msg + "','asymmetric','aaabbbaa','DSA')" +
        "return c:decrypt($e,'asymmetric','aaabbbaa','DSA')", msg);
  }

  @Test
  public void hmac1() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:hmac('message','key','hmacMd5', 'hex')",
        "\"4E4748E62B463521F6775FBF921234B5\"");
  }

  @Test
  public void generateSignatureSyntax() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:generate-signature(<a/>,'','','','','')");
  }

  @Test
  public void generateSignatureSyntax2() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:generate-signature(<a/>,'','','','','','')");
  }

  @Test
  public void generateSignatureSyntax3() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:generate-signature(<a/>,'','','','','',<a/>)");
  }

  @Test
  public void generateSignatureSyntax4() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:generate-signature(<a/>,'','','','','','',<a/>)");
  }

  @Test
  public void generateSignatureSyntax5() throws Exception {
    // TODO general syntax test equiv. FNDbTest.java check()
    Assert.fail();
  }

  @Test
  public void validateSignature1() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','','','',''))",
        "true");
  }

  @Test
  public void validateSignature1b() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(" +
        "c:generate-signature(" +
        "<a/>,'','SHA1','DSA-SHA1','','enveloped'))",
        "true");
  }

  @Test
  public void validateSignature1c() throws Exception {
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
  public void validateSignature2() throws Exception {
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
  public void validateSignature3Canonicalization() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>," +
        "'exclusive','','','',''))",
        "true");
  }

  @Test
  public void validateSignature4Canonicalization() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>," +
        "'exclusive-with-comments','','','',''))",
        "true");
  }

  @Test
  public void validateSignature5Canonicalization() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>," +
        "'inclusive','','','',''))",
        "true");
  }

  @Test
  public void validateSignature6Canonicalization() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>," +
        "'inclusive-with-comments','','','',''))",
        "true");
  }

  @Test
  public void validateSignature7DigestAlgorithm() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','SHA1','','',''))",
        "true");
  }

  @Test
  public void validateSignature8DigestAlgorithm() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','SHA256','','',''))",
        "true");
  }

  @Test
  public void validateSignature9DigestAlgorithm() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','SHA512','','',''))",
        "true");
  }

  @Test
  public void validateSignature10SignatureAlgorithm() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'',''," +
        "'DSA_SHA1','',''))",
        "true");
  }

  @Test
  public void validateSignature11SignatureAlgorithm() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'',''," +
        "'RSA_SHA1','',''))",
        "true");
  }

  @Test
  public void validateSignature12SignatureNamespace() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','','','prefix',''))",
        "true");
    Assert.fail();
  }

  @Test
  public void validateSignature13SignatureType() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','','',''," +
        "'enveloped'))",
        "true");
    Assert.fail();
  }

  @Test
  public void validateSignature14SignatureType() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','','',''," +
        "'enveloping'))",
        "true");
    Assert.fail();
  }

  @Test
  public void validateSignature15SignatureType() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','','',''," +
        "'detached'))",
        "true");
    Assert.fail();
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