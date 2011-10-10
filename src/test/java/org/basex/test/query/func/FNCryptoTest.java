package org.basex.test.query.func;

import org.basex.core.cmd.DropDB;
import static org.basex.query.func.Function.*;
import org.basex.test.query.AdvancedQueryTest;
import org.basex.util.Util;
import org.junit.AfterClass;
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
   * Checks FNCrypto functions for correct argument handling.
   */
  @Test
  public void checkFunctionArguments() {
    check(CRYPENCRYPT);
    check(CRYPDECRYPT);
    check(CRYPHMAC);
    check(CRYPGENSIG);
    check(CRYPVALSIG);
  }

  /**
   * Test method for crypto:encrypt and crypto:decrypt with symmetric keys.
   */
  @Test
  public void encryption1() {
    final String msg = "messagemessagemessagemessagemessagemessagemessage";

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

    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "let $e := c:encrypt('" + msg + "','symmetric','abababababababab'," +
        "'AES') return c:decrypt($e,'symmetric'," +
        "'abababababababab','AES')", msg);
  }

  /**
   * Tests the creation of a message authentication code for the md5 algorithm.
   */
  @Test
  public void hmacMD5defencoding() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:hmac('message','key','md5')",
        "TkdI5itGNSH2d1+/khI0tQ==");
  }

  /**
   * Tests the creation of a message authentication code for the md5 algorithm.
   */
  @Test
  public void hmacMD5hex() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:hmac('message','key','md5', 'hex')",
        "4E4748E62B463521F6775FBF921234B5");
  }

  /**
   * Tests the creation of a message authentication code for the md5 algorithm.
   */
  @Test
  public void hmacMD5base64() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:hmac('message','key','md5', 'base64')",
        "TkdI5itGNSH2d1+/khI0tQ==");
  }

  /**
   * Tests the creation of a message authentication code for the sha1 algorithm.
   */
  @Test
  public void hmacSHA1hex() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:hmac('message','key','sha1', 'hex')",
        "2088DF74D5F2146B48146CAF4965377E9D0BE3A4");
  }

  /**
   * Tests the creation of a message authentication code for the sha1 algorithm.
   */
  @Test
  public void hmacSHA1base64() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:hmac('message','key','sha1', 'base64')",
        "IIjfdNXyFGtIFGyvSWU3fp0L46Q=");
  }

  /**
   * Tests the creation of a message authentication code for the sha256
   * algorithm.
   */
  @Test
  public void hmacSHA256hex() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:hmac('message','key','sha256', 'hex')",
        "6E9EF29B75FFFC5B7ABAE527D58FDADB2FE42E7219011976917343065F58ED4A");
  }

  /**
   * Tests the creation of a message authentication code for the sha256
   * algorithm.
   */
  @Test
  public void hmacSHA256base64() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:hmac('message','key','sha256', 'base64')",
        "bp7ym3X//Ft6uuUn1Y/a2y/kLnIZARl2kXNDBl9Y7Uo=");
  }

  /**
   * Tests the creation of a message authentication code for the sha384
   * algorithm.
   */
  @Test
  public void hmacSHA384hex() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:hmac('message','key','sha384', 'hex')",
        "2088DF74D5F2146B48146CAF4965377E9D0BE3A4");
  }

  /**
   * Tests the creation of a message authentication code for the sha384
   * algorithm.
   */
  @Test
  public void hmacSHA384base64() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:hmac('message','key','sha384', 'base64')",
        "IIjfdNXyFGtIFGyvSWU3fp0L46Q=");
  }

  /**
   * Tests the creation of a message authentication code for the sha512
   * algorithm.
   */
  @Test
  public void hmacSHA512hex() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:hmac('message','key','sha512', 'hex')",
        "E477384D7CA229DD1426E64B63EBF2D36EBD6D7E669A6735424E72EA6C01D3F8" +
        "B56EB39C36D8232F5427999B8D1A3F9CD1128FC69F4D75B434216810FA367E98");
  }

  /**
   * Tests the creation of a message authentication code for the sha512
   * algorithm.
   */
  @Test
  public void hmacSHA512base64() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:hmac('message','key','sha512', 'base64')",
        "5Hc4TXyiKd0UJuZLY+vy0269bX5mmmc1Qk5y6mwB0/i1brOcNtgjL1QnmZuNGj+c0RK" +
        "Pxp9NdbQ0IWgQ+jZ+mA==");
  }

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test
  public void validateSignature1() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','','','',''))",
        "true");
  }

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test
  public void validateSignature1b() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(" +
        "c:generate-signature(" +
        "<a/>,'','SHA1','DSA_SHA1','','enveloped'))",
        "true");
  }

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test
  public void validateSignature1c() {
    final String input =
        "<a><Signature xmlns='http://www.w3.org/2000/09/xmldsig#'>" +
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

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test
  public void validateSignatureWithCanonicalization() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>," +
        "'exclusive','','','',''))",
        "true");
  }

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test
  public void validateSignatureWithCanonicalization2() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>," +
        "'exclusive-with-comments','','','',''))",
        "true");
  }

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test
  public void validateSignatureWithCanonicalization3() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>," +
        "'inclusive','','','',''))",
        "true");
  }

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test
  public void validateSignatureWithCanonicalization4() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>," +
        "'inclusive-with-comments','','','',''))",
        "true");
  }

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test
  public void validateSignatureWithDigestAlgorithm() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','SHA1','','',''))",
        "true");
  }

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test
  public void validateSignatureWithDigestAlgorithm2() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','SHA256','','',''))",
        "true");
  }

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test
  public void validateSignatureWithDigestAlgorithm3() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','SHA512','','',''))",
        "true");
  }

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test
  public void validateSignatureWithSignatureAlgorithm() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'',''," +
        "'DSA_SHA1','',''))",
        "true");
  }

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test
  public void validateSignatureWithSignatureAlgorithm2() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'',''," +
        "'RSA_SHA1','',''))",
        "true");
  }

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test
  public void validateSignatureWithSignatureNamespace3() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','','','prefix',''))",
        "true");
  }

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test
  public void validateSignatureWithSignatureType() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','','',''," +
        "'enveloped'))",
        "true");
  }

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test
  public void validateSignatureWithSignatureType2() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','','',''," +
        "'enveloping'))",
        "true");
  }

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test
  public void validateSignatureWithXPath() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a><n/><n/></a>," +
        "'','','',''," +
        "'','/a/n'))",
        "true");
  }

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test
  public void validateSignatureFullySpecified() {

    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a><n/></a>," +
        "'exclusive','SHA512','RSA_SHA1','myPrefix','enveloped','/a/n'))",
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