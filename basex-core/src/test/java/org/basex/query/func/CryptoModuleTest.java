package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import java.security.*;
import java.util.*;
import java.util.logging.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Cryptography Module.
 *
 * @author BaseX Team, BSD License
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

    query("let $e :=" + func.args(msg, "symmetric", "abababababababab", "AES") +
        "return" + func2.args(" $e", "symmetric", "abababababababab", "AES"), msg);
    query("let $e :=" + func.args(msg, "symmetric", "abababababababab", "aes") +
        "return" + func2.args(" $e", "symmetric", "abababababababab", "aes"), msg);
    query("let $e :=" + func.args(msg, "symmetric", "abababababababab", "AES-GCM") +
        "return" + func2.args(" $e", "symmetric", "abababababababab", "AES-GCM"), msg);
    query("let $e :=" + func.args(msg, "symmetric", "abababababababababababababababab", "AES-GCM") +
        "return" + func2.args(" $e", "symmetric", "abababababababababababababababab", "AES-GCM"),
        msg);
    // encryption results differ due to random initialization vectors
    query(func.args(msg, "symmetric", "abababababababab", "AES-GCM") + " = " +
        func.args(msg, "symmetric", "abababababababab", "AES-GCM"), false);

    // tampered data is rejected
    error("let $e :=" + func.args(msg, "symmetric", "abababababababab", "AES-GCM") +
        "return" + func2.args(" bin:not($e)", "symmetric", "abababababababab", "AES-GCM"),
        CX_BADPAD_X);
    error(func.args(msg, "symmetric", "abababababababab", "DES"), CX_INVALGO_X);
    error(func.args(msg, "symmetric", "abababababababab", "RSA"), CX_INVALGO_X);
    error(func.args(msg, "asymmetric", "abababababababab", "AES"), CX_INVALGO_X);
    error(func.args(msg, "unknown", "abababababababab", "AES"), CX_ENCTYP_X);
    error(func.args(msg, "symmetric", "ababab", "AES"), CX_KEYINV_X);
  }

  /**
   * Test method for encrypt and decrypt with asymmetric keys.
   * @throws GeneralSecurityException general security exception
   */
  @Test public void encryptionRsa() throws GeneralSecurityException {
    final Function func = _CRYPTO_ENCRYPT, func2 = _CRYPTO_DECRYPT;
    final String msg = "message";

    final KeyPair kp = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    final Base64.Encoder enc = Base64.getEncoder();
    final String pub = enc.encodeToString(kp.getPublic().getEncoded());
    final String priv = enc.encodeToString(kp.getPrivate().getEncoded());

    // DER keys
    query("let $e :=" + func.args(msg, "asymmetric", " xs:base64Binary('" + pub + "')", "RSA") +
        "return" + func2.args(" $e", "asymmetric", " xs:base64Binary('" + priv + "')", "RSA"),
        msg);
    // PEM keys
    final String pubPem = "-----BEGIN PUBLIC KEY-----\n" + pub + "\n-----END PUBLIC KEY-----\n";
    final String privPem = "-----BEGIN PRIVATE KEY-----\n" + priv + "\n-----END PRIVATE KEY-----\n";
    query("let $e :=" + func.args(msg, "asymmetric", pubPem, "RSA") +
        "return" + func2.args(" $e", "asymmetric", privPem, "RSA"), msg);

    error(func.args(msg, "asymmetric", "abababababababab", "RSA"), CX_KEYINV_X);
    error(func.args(msg, "asymmetric", privPem, "RSA"), CX_KEYINV_X);
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

  /** Tests the derivation of keys from passwords (test vectors from RFC 6070 and RFC 7914). */
  @Test public void pbkdf2() {
    final Function func = _CRYPTO_PBKDF2;

    query(func.args("password", "salt", 1, 32) + " => string()",
        "120FB6CFFCF8B32C43E7225256C4F837A86548C92CCC35480805987CB70BE17B");
    query(func.args("password", "salt", 1, 32, "SHA-256") + " => string()",
        "120FB6CFFCF8B32C43E7225256C4F837A86548C92CCC35480805987CB70BE17B");
    query(func.args("password", "salt", 1, 32, "sha256") + " => string()",
        "120FB6CFFCF8B32C43E7225256C4F837A86548C92CCC35480805987CB70BE17B");
    query(func.args("password", "salt", 4096, 32) + " => string()",
        "C5E478D59288C841AA530DB6845C4C8D962893A001CE4E11A4963873AA98134A");
    query(func.args("password", " xs:hexBinary('73616C74')", 1, 32) + " => string()",
        "120FB6CFFCF8B32C43E7225256C4F837A86548C92CCC35480805987CB70BE17B");
    query(func.args("password", "salt", 1, 20, "SHA-1") + " => string()",
        "0C60C80F961F0E71F3A9B524AF6012062FE037A6");
    query(func.args("password", "salt", 2, 20, "SHA1") + " => string()",
        "EA6C014DC72D6F8CCD1ED92ACE1D41F0D8DE8957");
    query(func.args("password", "salt", 1, 64, "SHA-512") + " => string() => string-length()",
        128);

    error(func.args("password", "salt", 1, 32, "unknown"), CX_INVHASH_X);
    error(func.args("password", "", 1, 32), CX_KEYINV_X);
    error(func.args("password", "salt", 0, 32), INVTYPE_X);
    error(func.args("password", "salt", 1, 0), INVTYPE_X);
  }
}
