package org.basex.query.func;

import static org.basex.query.func.Function.*;

import java.util.logging.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Cryptography Module.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Lukas Kircher
 */
public final class CryptoModuleTest extends SandboxTest {
  /** Set higher log level to avoid INFO output. */
  public CryptoModuleTest() {
    Logger.getLogger("com.sun.org.apache.xml.internal").setLevel(Level.WARNING);
  }

  /**
   * Test method for encrypt and decrypt with symmetric keys.
   */
  @Test public void encryption() {
    final Function func = _CRYPTO_ENCRYPT, func2 = _CRYPTO_DECRYPT;
    final String msg = "messagemessagemessagemessagemessagemessagemessage";

    query("let $e :=" + func.args(msg, "symmetric", "aaabbbaa", "DES") +
        "return" + func2.args(" $e", "symmetric", "aaabbbaa", "DES"), msg);
    query("let $e :=" + func.args(msg, "symmetric", "abababababababab", "AES") +
        "return" + func2.args(" $e", "symmetric", "abababababababab", "AES"), msg);
  }

  /** Tests the creation of message authentication codes for the md5 algorithm. */
  @Test public void hmacMD5() {
    final Function func = _CRYPTO_HMAC;
    final String msg = "message";

    query(func.args(msg, "key", "md5"), "TkdI5itGNSH2d1+/khI0tQ==");
    query(func.args(msg, "key", "md5", "base64"), "TkdI5itGNSH2d1+/khI0tQ==");
    query(func.args(msg, "key", "md5", "hex"), "4E4748E62B463521F6775FBF921234B5");
  }

  /** Tests the creation of message authentication codes for the sha1 algorithm. */
  @Test public void hmacSHA1() {
    final Function func = _CRYPTO_HMAC;
    final String msg = "message";

    query(func.args(msg, "key", "sha1", "base64"), "IIjfdNXyFGtIFGyvSWU3fp0L46Q=");
    query(func.args(msg, "key", "sha1", "hex"), "2088DF74D5F2146B48146CAF4965377E9D0BE3A4");
  }

  /** Tests the creation of message authentication codes for the sha256 algorithm. */
  @Test public void hmacSHA256() {
    final Function func = _CRYPTO_HMAC;
    final String msg = "message";

    query(func.args(msg, "key", "sha256", "base64"),
        "bp7ym3X//Ft6uuUn1Y/a2y/kLnIZARl2kXNDBl9Y7Uo=");
    query(func.args(msg, "key", "sha256", "hex"),
        "6E9EF29B75FFFC5B7ABAE527D58FDADB2FE42E7219011976917343065F58ED4A");
  }

  /** Tests the creation of message authentication codes for the sha384 algorithm. */
  @Test public void hmacSHA384() {
    final Function func = _CRYPTO_HMAC;
    final String msg = "message";

    query(func.args(msg, "key", "sha384", "base64"),
        "D9OuMje+mMZKB1tzlJifxnifMXiPraQurahe5mmL3i/q4gtmJxtnVEuQYsdzsthv");
    query(func.args(msg, "key", "sha384", "hex"),
        "0FD3AE3237BE98C64A075B7394989FC6789F31788FADA42EADA85EE6698BDE2F" +
        "EAE20B66271B67544B9062C773B2D86F");
  }

  /** Tests the creation of message authentication codes for the sha512 algorithm. */
  @Test public void hmacSHA512() {
    final Function func = _CRYPTO_HMAC;
    final String msg = "message";

    query(func.args(msg, "key", "sha512", "base64"),
        "5Hc4TXyiKd0UJuZLY+vy0269bX5mmmc1Qk5y6mwB0/i1brOcNtgjL1QnmZuNGj+c0RK" +
        "Pxp9NdbQ0IWgQ+jZ+mA==");
    query(func.args(msg, "key", "sha512", "hex"),
        "E477384D7CA229DD1426E64B63EBF2D36EBD6D7E669A6735424E72EA6C01D3F8" +
        "B56EB39C36D8232F5427999B8D1A3F9CD1128FC69F4D75B434216810FA367E98");
  }

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test public void validateSignature() {
    final Function func = _CRYPTO_VALIDATE_SIGNATURE, func2 = _CRYPTO_GENERATE_SIGNATURE;

    query(func.args(func2.args(" <a/>", "", "", "", "", "")), true);
    query(func.args(func2.args(" <a/>", "", "SHA1", "DSA_SHA1", "", "enveloped")), true);

    final String input =
        " <a><Signature xmlns='http://www.w3.org/2000/09/xmldsig#'>" +
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
    query(func.args(input), true);

    query(func.args(func2.args(" <a><n/></a>", "exclusive", "SHA512", "RSA_SHA1", "myPrefix",
        "enveloped", "/a/n")), true);
    query(func.args(func2.args(" <a/>", "exclusive", "", "", "", "")), true);
    query(func.args(func2.args(" <a/>", "exclusive-with-comments", "", "", "", "")), true);
    query(func.args(func2.args(" <a/>", "inclusive", "", "", "", "")), true);
    query(func.args(func2.args(" <a/>", "inclusive-with-comments", "", "", "", "")), true);
    query(func.args(func2.args(" <a/>", "", "SHA1", "", "", "")), true);
    query(func.args(func2.args(" <a/>", "", "SHA256", "", "", "")), true);
    query(func.args(func2.args(" <a/>", "", "SHA512", "", "", "")), true);
    query(func.args(func2.args(" <a/>", "", "", "DSA_SHA1", "", "")), true);
    query(func.args(func2.args(" <a/>", "", "", "RSA_SHA1", "", "")), true);
    query(func.args(func2.args(" <a/>", "", "", "", "prefix", "")), true);
    query(func.args(func2.args(" <a/>", "", "", "", "", "enveloped")), true);
    query(func.args(func2.args(" <a/>", "", "", "", "", "enveloping")), true);
    query(func.args(func2.args(" <a><n/><n/></a>", "", "", "", "", "", "/a/n")), true);
  }
}