package org.basex.query.func;

import static org.basex.query.func.Function.*;

import java.util.logging.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Cryptography Module.
 *
 * @author BaseX Team 2005-24, BSD License
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
}