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

    // JWK key
    query("let $e :=" + func.args(msg, "symmetric", " { 'kty': 'oct', 'k': " +
        "'YWJhYmFiYWJhYmFiYWJhYg' }", "AES") +
        "return" + func2.args(" $e", "symmetric", "abababababababab", "AES"), msg);
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

    // JWK keys
    query("let $e :=" + func.args(msg, "asymmetric", " map:remove(" + RSA_JWK + ", 'd')", "RSA") +
        "return" + func2.args(" $e", "asymmetric", RSA_JWK, "RSA"), msg);
    error(func.args(msg, "asymmetric", " map:put(" + RSA_JWK + ", 'use', 'sig')", "RSA"),
        CX_KEYALG_X);
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

  /** Payload of RFC 7515, Appendix A.1-A.3. */
  private static final String PAYLOAD =
      "eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cn" +
          "VlfQ";
  /** RSA key of RFC 7515, Appendix A.2. */
  private static final String RSA_JWK = " { 'kty': 'RSA', 'n': 'ofgWCuLjybRlzo0tZWJjNiuSfb4p4fAkd" +
      "_wWJcyQoTbji9k0l8W26mPddxHmfHQp-Vaw-4qPCJrcS2mJPMEzP1Pt0Bm4d4QlL-yRT-SFd2lZS-pCgNMsD1W_YpR" +
      "PEwOWvG6b32690r2jZ47soMZo9wGzjb_7OMg0LOL-bSf63kpaSHSXndS5z5rexMdbBYUsLA9e-KXBdQOS-UTo7WTBE" +
      "Ma2R2CapHg665xsmtdVMTBQY4uDZlxvb3qCo5ZwKh9kG4LT6_I5IhlJH7aGhyxXFvUK-DWNmoudF8NAco9_h9iaGNj" +
      "8q2ethFkMLs91kzk2PAcDTW9gb54h4FRWyuXpoQ', 'e': 'AQAB', 'd': 'Eq5xpGnNCivDflJsRQBXHx1hdR1k6" +
      "Ulwe2JZD50LpXyWPEAeP88vLNO97IjlA7_GQ5sLKMgvfTeXZx9SE-7YwVol2NXOoAJe46sui395IW_GO-pWJ1O0BkT" +
      "GoVEn2bKVRUCgu-GjBVaYLU6f3l9kJfFNS3E0QbVdxzubSu3Mkqzjkn439X0M_V51gfpRLI9JYanrC4D4qAdGcopV_" +
      "0ZHHzQlBjudU2QvXt4ehNYTCBr6XCLQUShb1juUO1ZdiYoFaFQT5Tw8bGUl_x_jTj3ccPDVZFD9pIuhLhBOneufuBi" +
      "B4cS98l2SR_RQyGWSeWjnczT0QU91p1DhOVRuOopznQ', 'p': '4BzEEOtIpmVdVEZNCqS7baC4crd0pqnRH_5IB3" +
      "jw3bcxGn6QLvnEtfdUdiYrqBdss1l58BQ3KhooKeQTa9AB0Hw_Py5PJdTJNPY8cQn7ouZ2KKDcmnPGBY5t7yLc1QlQ" +
      "5xHdwW1VhvKn-nXqhJTBgIPgtldC-KDV5z-y2XDwGUc', 'q': 'uQPEfgmVtjL0Uyyx88GZFF1fOunH3-7cepKmtH" +
      "4pxhtCoHqpWmT8YAmZxaewHgHAjLYsp1ZSe7zFYHj7C6ul7TjeLQeZD_YwD66t62wDmpe_HlB-TnBA-njbglfIsRLt" +
      "XlnDzQkv5dTltRJ11BKBBypeeF6689rjcJIDEz9RWdc', 'dp': 'BwKfV3Akq5_MFZDFZCnW-wzl-CCo83WoZvnLQ" +
      "wCTeDv8uzluRSnm71I3QCLdhrqE2e9YkxvuxdBfpT_PI7Yz-FOKnu1R6HsJeDCjn12Sk3vmAktV2zb34MCdy7cpdTh" +
      "_YVr7tss2u6vneTwrA86rZtu5Mbr1C1XsmvkxHQAdYo0', 'dq': 'h_96-mK1R_7glhsum81dZxjTnYynPbZpHziZ" +
      "jeeHcXYsXaaMwkOlODsWa7I9xXDoRwbKgB719rrmI2oKr6N3Do9U0ajaHF-NKJnwgjMd2w9cjz3_-kyNlxAr2v4IKh" +
      "GNpmM5iIgOS1VZnOZ68m6_pbLBSp3nssTdlqvd0tIiTHU', 'qi': 'IYd7DHOhrWvxkwPQsRM2tOgrjbcrfvtQJip" +
      "d-DlcxyVuuM9sQLdgjVk2oy26F0EmpScGLq2MowX7fhd_QJQ3ydy5cY7YIBi87w93IKLEdfnbJtoOPLUW0ITrJReOg" +
      "o1cq9SbsxYawBgfp_gh6A5603k2-ZQwVK0JKSHuLFkuQ3U' }";
  /** EC P-256 key of RFC 7515, Appendix A.3. */
  private static final String EC_JWK = " { 'kty': 'EC', 'crv': 'P-256', 'x': 'f83OJ3D2xF1Bg8vub9t" +
      "Le1gHMzV76e8Tus9uPHvRVEU', 'y': 'x_FEzRu9m36HLN_tue659LNpXW6pCyStikYjKIWI5a0', 'd': 'jpsQn" +
      "nGQmL-YBIffH1136cspYG6-0iY7X1fCE9-E9LI' }";
  /** Ed25519 key of RFC 8037, Appendix A.1. */
  private static final String ED_JWK = " { 'kty': 'OKP', 'crv': 'Ed25519', 'd': 'nWGxne_9WmC6hEr0" +
      "kuwsxERJxWl7MmkZcDusAxyuf2A', 'x': '11qYAYKxCrfVS_7TyWQHOg7hcvPapiMlrwIaaPcHURo' }";
  /** HMAC SHA-256 algorithm. */
  private static final String HS256 = " { 'name': 'HMAC', 'hash': 'SHA-256' }";
  /** RSASSA-PKCS1-v1_5 SHA-256 algorithm. */
  private static final String RS256 = " { 'name': 'RSASSA-PKCS1-v1_5', 'hash': 'SHA-256' }";
  /** ECDSA SHA-256 algorithm. */
  private static final String ES256 = " { 'name': 'ECDSA', 'hash': 'SHA-256' }";

  /** Creates and verifies MACs (test vectors from RFC 4231 and RFC 7515, Appendix A.1). */
  @Test public void signHmac() {
    final Function func = _CRYPTO_SIGN, func2 = _CRYPTO_VERIFY;

    // RFC 4231, test case 2
    final String hex = " => xs:hexBinary() => string()";
    final String data = "what do ya want for nothing?";
    query(func.args(data, "Jefe", HS256) + hex,
        "5BDCC146BF60754E6A042426089575C75A003F089D2739839DEC58B964EC3843");
    query(func.args(data, "Jefe", " { 'name': 'hmac', 'hash': 'sha384' }") + hex,
        "AF45D2E376484031617F78D2B58A6B1B9C7EF464F5A01B47E42EC3736322445E" +
        "8E2240CA5E69E2C78B3239ECFAB21649");
    query(func.args(data, "Jefe", " { 'name': 'HMAC', 'hash': 'SHA512' }") + hex,
        "164B7A7BFCF819E2E395FBE73B56E0A387BD64222E831FD610270CD7EA250554" +
        "9758BF75C05A994A6D034F65F8F0E6FDCAEAB1A34D4A6B4B636E070A38BCE737");
    // RFC 4231, test case 1: binary key and data
    query(func.args(" xs:hexBinary('4869205468657265')",
        " xs:hexBinary('0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b')", HS256) + hex,
        "B0344C61D8DB38535CA8AFCEAF0BF12B881DC200C9833DA726E9376C2E32CFF7");

    // RFC 7515, A.1: JWK key
    final String input = "eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9." + PAYLOAD;
    final String jwk = " { 'kty': 'oct', 'k': " +
        "'AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow'" +
            " }";
    final String sig = " bin:from-base64url('dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk')";
    query(func.args(input, jwk, HS256) + " = " + sig, true);
    query(func2.args(input, sig, jwk, HS256), true);
    query(func2.args(input + "x", sig, jwk, HS256), false);
    query(func2.args(input, " xs:hexBinary('00')", jwk, HS256), false);
    query(func2.args(data, func.args(data, "Jefe", HS256), "Jefe", HS256), true);

    // JWK members
    query(func2.args(input, sig, " map:put(" + jwk + ", 'alg', 'HS256')", HS256), true);
    query(func2.args(input, sig, " map:put(" + jwk + ", 'use', 'sig')", HS256), true);
    query(func2.args(input, sig, " map:put(" + jwk + ", 'key_ops', ('sign', 'verify'))", HS256),
        true);
    query(func2.args(input, sig, " map:put(" + jwk + ", 'key_ops', parse-json('[\"verify\"]'))",
        HS256), true);
    error(func2.args(input, sig, " map:put(" + jwk + ", 'alg', 'HS512')", HS256), CX_KEYALG_X);
    error(func2.args(input, sig, " map:put(" + jwk + ", 'alg', 'RS256')", HS256), CX_KEYALG_X);
    error(func2.args(input, sig, " map:put(" + jwk + ", 'use', 'enc')", HS256), CX_KEYALG_X);
    error(func2.args(input, sig, " map:put(" + jwk + ", 'key_ops', 'sign')", HS256), CX_KEYALG_X);
    error(func2.args(input, sig, " map:remove(" + jwk + ", 'k')", HS256), CX_KEYINV_X);
    error(func2.args(input, sig, " map:put(" + jwk + ", 'k', '***')", HS256), CX_KEYINV_X);

    // algorithms
    error(func.args(data, "Jefe", "HMAC"), CX_INVALGO_X);
    error(func.args(data, "Jefe", "HS256"), CX_INVALGO_X);
    error(func.args(data, "Jefe", " { 'hash': 'SHA-256' }"), CX_INVALGO_X);
    error(func.args(data, "Jefe", " { 'name': 'HMAC', 'hash': 'MD5' }"), CX_INVHASH_X);
    error(func.args(data, "Jefe", " { 'name': 'HMAC', 'hash': 'SHA-256', 'x': 1 }"),
        INVALIDOPTION_X);
    error(func.args(data, "Jefe", " { 'name': 'HMAC', 'hash': 'SHA-256', 'salt-length': 1 }"),
        CX_INVALGO_X);
    // key mismatches
    error(func.args(data, "", HS256), CX_KEYINV_X);
    // secrets with incomplete PEM blocks
    query(func.args(data, "my-----BEGIN-secret", HS256) + " => bin:length()", 32);
    query(func.args(data, "-----BEGIN X-----QUJD-----END X-----", HS256) + " => bin:length()",
        32);
    error(func.args(data, RSA_JWK, HS256), CX_KEYALG_X);
    error(func.args(data, jwk, RS256), CX_KEYALG_X);
    error(func.args(data, "-----BEGIN PUBLIC KEY-----\nAAAA\n-----END PUBLIC KEY-----", HS256),
        CX_KEYALG_X);
  }

  /** Creates and verifies RSA signatures (test vectors from RFC 7515, Appendix A.2). */
  @Test public void signRsa() {
    final Function func = _CRYPTO_SIGN, func2 = _CRYPTO_VERIFY;
    final String input = "eyJhbGciOiJSUzI1NiJ9." + PAYLOAD;
    final String sig = " bin:from-base64url('cC4hiUPoj9Eetdgtv3hF80EGrhuB__dzERat0XF9g2VtQgr9PJbu" +
        "3XOiZj5RZmh7AAuHIm4Bh-0Qc_lF5YKt_O8W2Fp5jujGbds9uJdbF9CUAr7t1dnZcAcQjbKBYNX4BAynRFdiuB--" +
        "f_nZLgrnbyTyWzO75vRK5h6xBArLIARNPvkSjtQBMHlb1L07Qe7K0GarZRmB_eSN9383LcOLn6_dO--xi12jzDwu" +
        "sC-eOkHWEsqtFZESc6BfI7noOPqvhJ1phCnvWh6IeYI2w9QOYEUipUTI8np6LbgGY9Fs98rqVt5AXLIhWkWywlVm" +
        "tVrBp0igcN_IoypGlUPQGe77Rw')";
    final String pub = " map:remove(" + RSA_JWK + ", ('d', 'p', 'q', 'dp', 'dq', 'qi'))";

    // deterministic signature, CRT and non-CRT private key
    query(func.args(input, RSA_JWK, RS256) + " = " + sig, true);
    query(func.args(input, " map:remove(" + RSA_JWK + ", ('p', 'q', 'dp', 'dq', 'qi'))", RS256) +
        " = " + sig, true);
    query(func2.args(input, sig, pub, RS256), true);
    query(func2.args(input, sig, RSA_JWK, RS256), true);
    query(func2.args(input, sig, " map:put(" + pub + ", 'alg', 'RS256')", RS256), true);
    query(func2.args(input + "x", sig, pub, RS256), false);
    query(func2.args(input, " xs:hexBinary('00')", pub, RS256), false);
    query(func2.args(input, sig, pub, " { 'name': 'RSASSA-PKCS1-v1_5', 'hash': 'SHA-512' }"),
        false);

    // RSA-PSS: randomized, round trip
    for(final String pss : new String[] {
      " { 'name': 'RSA-PSS', 'hash': 'SHA-256' }",
      " { 'name': 'RSA-PSS', 'hash': 'SHA-384', 'salt-length': 0 }"
    }) {
      query(func2.args(input, func.args(input, RSA_JWK, pss), pub, pss), true);
    }
    query(func2.args(input, func.args(input, RSA_JWK, " { 'name': 'RSA-PSS', 'hash': 'SHA-256' }"),
        pub, " { 'name': 'RSA-PSS', 'hash': 'SHA-256', 'salt-length': 0 }"), false);
    error(func.args(input, RSA_JWK, " { 'name': 'RSA-PSS', 'hash': 'SHA-256', 'salt-length': -1 }"),
        CX_INVALGO_X);
    error(func.args(input, RSA_JWK, " { 'name': 'RSA-PSS', 'hash': 'SHA-256', " +
        "'salt-length': 1000 }"), CX_KEYINV_X);

    // errors
    error(func.args(input, pub, RS256), CX_KEYINV_X);
    error(func.args(input, "secret", RS256), CX_KEYINV_X);
    error(func.args(input, " xs:hexBinary('00')", RS256), CX_KEYINV_X);
    error(func2.args(input, sig, EC_JWK, RS256), CX_KEYALG_X);
    error(func2.args(input, sig, " map:put(" + pub + ", 'alg', 'PS256')", RS256), CX_KEYALG_X);
    error(func2.args(input, sig, " map:remove(" + pub + ", 'n')", RS256), CX_KEYINV_X);
    error(func2.args(input, sig, " map:put(" + pub + ", 'n', 1)", RS256), CX_KEYINV_X);
  }

  /**
   * Creates and verifies signatures with PEM, DER and certificate keys.
   * @throws Exception exception
   */
  @Test public void signPem() throws Exception {
    final Function func = _CRYPTO_SIGN, func2 = _CRYPTO_VERIFY;
    final String msg = "message";

    final KeyPair rsa = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    final byte[] pub = rsa.getPublic().getEncoded(), priv = rsa.getPrivate().getEncoded();
    final String pubPem = pem("PUBLIC KEY", pub), privPem = pem("PRIVATE KEY", priv);

    final String sig = func.args(msg, privPem, RS256);
    query(func2.args(msg, sig, pubPem, RS256), true);
    query(func2.args(msg, func.args(msg, b64(rsa.getPrivate()), RS256), b64(rsa.getPublic()),
        RS256), true);
    query(func2.args(msg + "x", sig, pubPem, RS256), false);
    error(func.args(msg, pubPem, RS256), CX_KEYINV_X);
    error(func2.args(msg, sig, privPem, RS256), CX_KEYINV_X);
    error(func2.args(msg, sig, pubPem, ES256), CX_KEYALG_X);
    // PKCS #1 keys: strip SPKI and PKCS #8 headers (offsets valid for two-byte DER lengths)
    final String pub1 = pem("RSA PUBLIC KEY", Arrays.copyOfRange(pub, 24, pub.length));
    final String priv1 = pem("RSA PRIVATE KEY", Arrays.copyOfRange(priv, 26, priv.length));
    query(func2.args(msg, func.args(msg, priv1, RS256), pub1, RS256), true);
    query(func2.args(msg, sig, pub1, RS256), true);
    error(func.args(msg, pub1, RS256), CX_KEYINV_X);
    error(func2.args(msg, sig, pub1, ES256), CX_KEYALG_X);
    error(func2.args(msg, sig, pubPem.replace("PUBLIC", "RSA PUBLIC"), RS256), CX_KEYINV_X);
    // PEM with preamble, PEM as binary
    query(func2.args(msg, sig, "Bag Attributes\n" + pubPem, RS256), true);
    query(func2.args(msg, sig, " bin:encode-string('" + pubPem + "')", RS256), true);

    // certificates (PEM, DER), created with OpenSSL
    final String ecKey = "-----BEGIN PRIVATE KEY-----\n" +
        "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgts4IZLngz/3WuLm3\n" +
        "dzy8PEr+wjmHdy5G6uJsJcyW4RmhRANCAAQ50kA+ezFUA0EwlrifAsHSAg5m0zql\n" +
        "ORdgY9U27isAksWdclQV8j1acHIkDgzupcFKQUOY9yu1QFn4uAD0z9yM\n" +
        "-----END PRIVATE KEY-----\n";
    final String cert =
        "MIIBdzCCAR2gAwIBAgIUInyBzsfNIlKmLD2L57+6HNidKvQwCgYIKoZIzj0EAwIw" +
        "EDEOMAwGA1UEAwwFQmFzZVgwIBcNMjYxMTIzMTUzMTI1WhgPMjEyNjEwMzAxNTMx" +
        "MjVaMBAxDjAMBgNVBAMMBUJhc2VYMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE" +
        "OdJAPnsxVANBMJa4nwLB0gIOZtM6pTkXYGPVNu4rAJLFnXJUFfI9WnByJA4M7qXB" +
        "SkFDmPcrtUBZ+LgA9M/cjKNTMFEwHQYDVR0OBBYEFPx14gU2B8D6JLsKmAbNXsM7" +
        "jnQCMB8GA1UdIwQYMBaAFPx14gU2B8D6JLsKmAbNXsM7jnQCMA8GA1UdEwEB/wQF" +
        "MAMBAf8wCgYIKoZIzj0EAwIDSAAwRQIhAMJsqkP3ir9qmihiQvp2X/WK4m5g0Nbn" +
        "pxJwB2MMNZCbAiAfn1ixFXPV/eGqI4S5hFrO/A55D4zpE367B7n5MNEDVA==";
    final String certPem = pem("CERTIFICATE", Base64.getDecoder().decode(cert));
    final String certDer = " xs:base64Binary('" + cert + "')";
    final String ecSig = func.args(msg, ecKey, ES256);
    query(func2.args(msg, ecSig, certPem, ES256), true);
    query(func2.args(msg, ecSig, certDer, ES256), true);
    error(func2.args(msg, ecSig, certDer, RS256), CX_KEYALG_X);
    error(func.args(msg, certPem, ES256), CX_KEYINV_X);
    error(func2.args(msg, sig, privPem.replace("PRIVATE", "ENCRYPTED PRIVATE"), RS256),
        CX_KEYINV_X);

    // EC and Ed25519 keys
    final KeyPair ec = KeyPairGenerator.getInstance("EC").generateKeyPair();
    final String ecPub = b64(ec.getPublic()), ecPriv = b64(ec.getPrivate());
    final String der = " { 'name': 'ECDSA', 'hash': 'SHA-256', 'signature-format': 'der' }";
    query(func2.args(msg, func.args(msg, ecPriv, ES256), ecPub, ES256), true);
    query(func2.args(msg, func.args(msg, ecPriv, der), ecPub, der), true);
    query(func2.args(msg, func.args(msg, ecPriv, der), ecPub, ES256), false);
    error(func.args(msg, ecPriv, RS256), CX_KEYALG_X);
    error(func.args(msg, ecPriv, "Ed25519"), CX_KEYALG_X);

    final KeyPair ed = KeyPairGenerator.getInstance("Ed448").generateKeyPair();
    final String edPub = b64(ed.getPublic()), edPriv = b64(ed.getPrivate());
    query(func2.args(msg, func.args(msg, edPriv, "Ed448"), edPub, "ed448"), true);
    error(func2.args(msg, func.args(msg, edPriv, "Ed448"), edPub, "Ed25519"), CX_KEYALG_X);
  }

  /** Verifies ECDSA signatures (test vectors from RFC 7515, Appendix A.3 and A.4). */
  @Test public void signEcdsa() {
    final Function func = _CRYPTO_SIGN, func2 = _CRYPTO_VERIFY;

    final String input = "eyJhbGciOiJFUzI1NiJ9." + PAYLOAD;
    final String sig = " bin:from-base64url('DtEhU3ljbEg8L38VWAfUAqOyKAM6-Xx-F4GawxaepmXFCgfTjDxw" +
        "5djxLa8ISlSApmWQxfKTUJqPP3-Kg6NU1Q')";
    final String pub = " map:remove(" + EC_JWK + ", 'd')";
    query(func2.args(input, sig, pub, ES256), true);
    query(func2.args(input + "x", sig, pub, ES256), false);
    query(func2.args(input, sig, pub, " { 'name': 'ECDSA', 'hash': 'SHA-256', " +
        "'signature-format': 'der' }"), false);
    // randomized signatures: round trip
    query(func2.args(input, func.args(input, EC_JWK, ES256), pub, ES256), true);

    final String input2 = "eyJhbGciOiJFUzUxMiJ9.UGF5bG9hZA";
    final String sig2 = " bin:from-base64url('AdwMgeerwtHoh-l192l60hp9wAHZFVJbLfD_UxMi70cwnZOYaRI" +
        "1bKPWROc-mZZqwqT2SI-KGDKB34XO0aw_7XdtAG8GaSwFKdCAPZgoXD2YBJZCPEX3xKpRwcdOO8KpEHwJjyqOgzD" +
        "O7iKvU8vcnwNrmxYbSW9ERBXukOXolLzeO_Jn')";
    final String jwk2 = " { 'kty': 'EC', 'crv': 'P-521', 'x': 'AekpBQ8ST8a8VcfVOTNl353vSrDCLLJXmP" +
        "k06wTjxrrjcBpXp5EOnYG_NjFZ6OvLFV1jSfS9tsz4qUxcWceqwQGk', 'y': 'ADSmRA43Z1DSNx_RvcLI87cdL" +
        "07l6jQyyBXMoxVg_l2Th-x3S1WDhjDly79ajL4Kkd0AZMaZmh9ubmf63e3kyMj2' }";
    final String es512 = " { 'name': 'ECDSA', 'hash': 'SHA-512' }";
    query(func2.args(input2, sig2, jwk2, es512), true);
    query(func2.args(input2, sig2, jwk2, ES256), false);

    error(func2.args(input, sig, " map:put(" + pub + ", 'crv', 'P-192')", ES256), CX_KEYINV_X);
    error(func2.args(input, sig, " map:put(" + pub + ", 'alg', 'ES512')", ES256), CX_KEYALG_X);
    error(func2.args(input, sig, pub, " { 'name': 'ECDSA', 'hash': 'SHA-256', " +
        "'signature-format': 'asn1' }"), INVALIDOPTIONVALUE_X);
    error(func.args(input, pub, ES256), CX_KEYINV_X);
    error(func.args(input, EC_JWK, RS256), CX_KEYALG_X);
  }

  /** Creates and verifies Ed25519 signatures (test vectors from RFC 8037, Appendix A.4). */
  @Test public void signEd25519() {
    final Function func = _CRYPTO_SIGN, func2 = _CRYPTO_VERIFY;
    final String input = "eyJhbGciOiJFZERTQSJ9.RXhhbXBsZSBvZiBFZDI1NTE5IHNpZ25pbmc";
    final String sig = " bin:from-base64url('hgyY0il_MGCjP0JzlnLWG1PPOt7-09PGcvMg3AIbQR6dWbhijcNR" +
        "4ki4iylGjg5BhVsPt9g7sVvpAr_MuM0KAg')";
    final String pub = " map:remove(" + ED_JWK + ", 'd')";

    query(func.args(input, ED_JWK, "Ed25519") + " = " + sig, true);
    query(func2.args(input, sig, pub, "Ed25519"), true);
    query(func2.args(input, sig, pub, " { 'name': 'ed25519' }"), true);
    query(func2.args(input, sig, " map:put(" + pub + ", 'alg', 'EdDSA')", "Ed25519"), true);
    query(func2.args(input + "x", sig, pub, "Ed25519"), false);
    query(func2.args(input, " xs:hexBinary('00')", pub, "Ed25519"), false);

    error(func2.args(input, sig, pub, "Ed448"), CX_KEYALG_X);
    // public key with an invalid length
    error(func2.args(input, sig, " map:put(" + pub + ", 'x', 'AAAA' || (" + pub + ")?x)",
        "Ed25519"), CX_KEYINV_X);
    error(func2.args(input, sig, pub, " { 'name': 'Ed25519', 'hash': 'SHA-256' }"), CX_INVALGO_X);
    error(func2.args(input, sig, " map:put(" + pub + ", 'crv', 'X25519')", "Ed25519"),
        CX_KEYALG_X);
    error(func2.args(input, sig, " map:put(" + pub + ", 'kty', 'EC')", "Ed25519"), CX_KEYALG_X);
    error(func.args(input, pub, "Ed25519"), CX_KEYINV_X);
  }

  /** Tests encryption with WebCrypto algorithms (test vector from the GCM specification). */
  @Test public void encryptionWebCrypto() {
    final Function func = _CRYPTO_ENCRYPT, func2 = _CRYPTO_DECRYPT;
    final String key = " { 'kty': 'oct', 'k': 'AAAAAAAAAAAAAAAAAAAAAA' }";
    final String gcm = " { 'name': 'AES-GCM', 'iv': xs:hexBinary('000000000000000000000000') }";

    // test case 2: explicit IV, ciphertext and tag without IV
    query(func.args(" xs:hexBinary('00000000000000000000000000000000')", "symmetric", key, gcm) +
        " => xs:hexBinary() => string()",
        "0388DACE60B6A392F328C2B971B2FE78AB6E47D42CEC13BDF53A67B21257BDDF");
    final String enc = func.args("message", "symmetric", key, gcm);
    query(func2.args(enc, "symmetric", key, gcm), "message");
    query(func2.args(enc, "symmetric", key, gcm, " { 'binary': true() }") + " => string()",
        "bWVzc2FnZQ==");
    query(func2.args(enc, "symmetric", key, gcm, " { 'binary': true() }") + " instance of " +
        "xs:base64Binary", true);

    // additional data, tag length
    final String aad = " map:put(" + gcm + ", 'additional-data', 'header')";
    query(func2.args(func.args("m", "symmetric", key, aad), "symmetric", key, aad), "m");
    error(func2.args(func.args("m", "symmetric", key, aad), "symmetric", key, gcm), CX_BADPAD_X);
    for(final int tl : new int[] { -8, 64, 100, 136 }) {
      error(func.args("m", "symmetric", key, " map:put(" + gcm + ", 'tag-length', " + tl + ")"),
          CX_INVALGO_X);
    }
    final String tag = " map:put(" + gcm + ", 'tag-length', 96)";
    query(func.args("", "symmetric", key, tag) + " => bin:length()", 12);
    query(func2.args(func.args("m", "symmetric", key, tag), "symmetric", key, tag), "m");

    // generated IV: prepended to the result, compatible with legacy names
    for(final String alg : new String[] { "AES-GCM", "AES-CBC" }) {
      query(func2.args(func.args("m", "symmetric", key, alg), "symmetric", key,
          " { 'name': '" + alg + "' }"), "m");
    }
    query(func2.args(func.args("m", "symmetric", key, "AES"), "symmetric", key, "AES-CBC"),
        "m");
    query(func.args("", "symmetric", key, "AES-CBC") + " => bin:length()", 32);
    final String cbc = " { 'name': 'AES-CBC', 'iv': xs:hexBinary('" + "00".repeat(16) + "') }";
    query(func.args("", "symmetric", key, cbc) + " => bin:length()", 16);
    query(func2.args(func.args("m", "symmetric", key, cbc), "symmetric", key, cbc), "m");

    // RSA-OAEP with SHA-256 (MGF1 with SHA-256)
    final String oaep = " { 'name': 'RSA-OAEP', 'hash': 'SHA-256' }";
    final String pub = " map:remove(" + RSA_JWK + ", 'd')";
    query(func2.args(func.args("m", "asymmetric", pub, oaep), "asymmetric", RSA_JWK, oaep), "m");
    error(func2.args(func.args("m", "asymmetric", pub, oaep), "asymmetric", RSA_JWK, "RSA"),
        CX_BADPAD_X);
    query(func2.args(func.args("m", "asymmetric", " map:put(" + pub + ", 'alg', 'RSA-OAEP-256')",
        oaep), "asymmetric", RSA_JWK, oaep), "m");
    error(func.args("m", "asymmetric", " map:put(" + pub + ", 'alg', 'RSA-OAEP')", oaep),
        CX_KEYALG_X);
    query(func.args("m", "asymmetric", " map:put(" + pub + ", 'alg', 'RS256')", "RSA") +
        " => bin:length()", 256);
    // JWK algorithms of AES keys exported by the Web Cryptography API
    query(func.args("m", "symmetric", " map:put(" + key + ", 'alg', 'A128CBC')", "AES-CBC") +
        " => bin:length()", 32);
    error(func.args("m", "symmetric", " map:put(" + key + ", 'alg', 'A128GCM')", "AES-CBC"),
        CX_KEYALG_X);

    // random IVs: encryptions of the same input are never merged
    query("(1 to 2) ! " + func.args("m", "symmetric", key, "AES-GCM") + " => distinct-values()" +
        " => count()", 2);

    error(func.args("m", "symmetric", key, " { 'name': 'AES-CBC', 'hash': 'SHA-256' }"),
        CX_INVALGO_X);
    error(func.args("m", "asymmetric", pub, " { 'name': 'RSA-OAEP' }"), CX_INVALGO_X);
    error(func.args("m", "asymmetric", pub, " { 'name': 'RSA', 'hash': 'SHA-256' }"),
        CX_INVALGO_X);
    error(func.args("m", "symmetric", key, "HMAC"), CX_INVALGO_X);
    error(func.args("m", "symmetric", key, " { 'name': 'AES-CBC', 'iv': xs:hexBinary('00') }"),
        CX_INVALGO_X);
    error(_CRYPTO_SIGN.args("m", key, "AES-GCM"), CX_INVALGO_X);
  }

  /** Tests key derivation (test vectors from RFC 5869 and RFC 7748). */
  @Test public void derive() {
    final Function func = _CRYPTO_DERIVE;

    // HKDF, test case 1
    query(func.args(" xs:hexBinary('" + "0b".repeat(22) + "')", " { 'name': 'HKDF', " +
        "'hash': 'SHA-256', 'salt': xs:hexBinary('000102030405060708090a0b0c'), " +
        "'info': xs:hexBinary('f0f1f2f3f4f5f6f7f8f9') }", 42) + " => xs:hexBinary() => string()",
        "3CB25F25FAACD57A90434F64D0362F2A2D2D0A90CF1A5A4C5DB02D56ECC4C5BF" +
        "34007208D5B887185865");
    // HKDF, test case 3: no salt and info
    query(func.args(" xs:hexBinary('" + "0b".repeat(22) + "')",
        " { 'name': 'HKDF', 'hash': 'SHA-256' }", 42) + " => xs:hexBinary() => string()",
        "8DA4E775A563C18F715F802A063C5A31B8A11F5C5EE1879EC3454E5F3C738D2D" +
        "9D201395FAA4B61A96C8");
    query(func.args("secret", " { 'name': 'HKDF', 'hash': 'SHA-512' }") + " => bin:length()",
        64);

    // X25519, RFC 7748, Section 6.1
    final String alice = " { 'kty': 'OKP', 'crv': 'X25519', " +
        "'d': 'dwdtCnMYpX08FsFyUbJmRd9ML4frwJkqsXf7pR25LCo', " +
        "'x': 'hSDwCYkwp1R0i33ctD73Wg2_Og0mOBr066SpjqqbTmo' }";
    final String bob = " { 'kty': 'OKP', 'crv': 'X25519', " +
        "'x': '3p7bfXt9wbTTW2HC7OQ1Nz-DQ8hbeGdNrfx-FG-IK08' }";
    query(func.args(alice, " { 'name': 'X25519', 'public': " + bob + " }") +
        " => xs:hexBinary() => string()",
        "4A5D9D5BA4CE2DE1728E3BF480350F25E07E21C947D19E3376F09B3C1E161742");
    query(func.args(alice, " { 'name': 'X25519', 'public': " + bob + " }", 16) +
        " => bin:length()", 16);

    // ECDH and X25519 with generated keys
    final String[][] algs = {
      { " { 'name': 'ECDH', 'named-curve': 'P-384' }", "ECDH" },
      { " 'X25519'", "X25519" }
    };
    for(final String[] alg : algs) {
      query("let $a := " + _CRYPTO_GENERATE_KEY.args(alg[0]) + " " +
          "let $b := " + _CRYPTO_GENERATE_KEY.args(alg[0]) + " return " +
          func.args(" $a?private", " { 'name': '" + alg[1] + "', 'public': $b?public }") + " = " +
          func.args(" $b?private", " { 'name': '" + alg[1] + "', 'public': $a?public }"), true);
    }

    error(func.args("secret", " { 'name': 'HKDF' }"), CX_INVALGO_X);
    error(func.args("secret", " { 'name': 'HKDF', 'hash': 'SHA-256' }", 255 * 32 + 1),
        CX_INVALGO_X);
    error(func.args(alice, " { 'name': 'X25519' }"), CX_INVALGO_X);
    error(func.args(alice, " { 'name': 'X25519', 'public': " + bob + " }", 33), CX_INVALGO_X);
    error(func.args(alice, " { 'name': 'ECDH', 'public': " + bob + " }"), CX_KEYALG_X);
    error(func.args(alice, "Ed25519"), CX_INVALGO_X);
    error(_CRYPTO_GENERATE_KEY.args(" 'HKDF'"), CX_INVALGO_X);
    error(func.args(alice, " { 'name': 'ECDH', 'named-curve': 'P-256', 'public': " + bob + " }"),
        CX_INVALGO_X);
    error(func.args("secret", " { 'name': 'HKDF', 'hash': 'SHA-256' }", 3000000000L), RANGE_X);

    // JWK members: key_ops of the Web Cryptography API, any alg for HKDF
    query(func.args(" map:put(" + alice + ", 'key_ops', ['deriveKey'])", " { 'name': 'X25519', " +
        "'public': " + bob + " }") + " => bin:length()", 32);
    query(func.args(" { 'kty': 'oct', 'k': 'c2VjcmV0', 'alg': 'HKDF' }",
        " { 'name': 'HKDF', 'hash': 'SHA-256' }") + " => bin:length()", 32);
  }

  /** Tests the generation of keys. */
  @Test public void generateKey() {
    final Function func = _CRYPTO_GENERATE_KEY, sign = _CRYPTO_SIGN, verify = _CRYPTO_VERIFY;
    final String es384 = " { 'name': 'ECDSA', 'named-curve': 'P-384' }";
    final String ps256 = " { 'name': 'RSA-PSS', 'modulus-length': 2048 }";

    // key pairs: sign with the private key, verify with the public key
    final String[][] algs = {
      { RS256, RS256 },
      { ps256, " { 'name': 'RSA-PSS', 'hash': 'SHA-256' }" },
      { es384, " { 'name': 'ECDSA', 'hash': 'SHA-384' }" },
      { " 'Ed25519'", " 'Ed25519'" },
      { " 'Ed448'", " 'Ed448'" }
    };
    for(final String[] alg : algs) {
      query("let $k := " + func.args(alg[0]) + " return " +
          verify.args("m", sign.args("m", " $k?private", alg[1]), " $k?public", alg[1]), true);
    }
    // generation parameters are rejected by other functions, operation parameters by generation
    error(sign.args("m", " " + func.args(es384) + "?private",
        " { 'name': 'ECDSA', 'hash': 'SHA-384', 'named-curve': 'P-384' }"), CX_INVALGO_X);
    error(func.args(" { 'name': 'ECDSA', 'named-curve': 'P-384', 'signature-format': 'der' }"),
        CX_INVALGO_X);
    query(func.args(es384) + "?public => map:keys()", "kty\ncrv\nx\ny");
    query(func.args(es384) + "?public?crv", "P-384");
    query(func.args(" 'Ed25519'") + "?private => map:keys()", "kty\ncrv\nx\nd");
    query(func.args(RS256) + "?private => map:keys()", "kty\nn\ne\nd\np\nq\ndp\ndq\nqi");
    query(func.args(RS256) + "?public?n => bin:from-base64url() => bin:length()", 256);

    // secret keys: default length is the block size of the hash function
    query(func.args(HS256) + "?k => bin:from-base64url() => bin:length()", 64);
    query(func.args(" { 'name': 'HMAC', 'hash': 'SHA-512' }") +
        "?k => bin:from-base64url() => bin:length()", 128);
    query(func.args(" { 'name': 'HMAC', 'hash': 'SHA-256', 'length': 256 }") +
        "?k => bin:from-base64url() => bin:length()", 32);
    query(func.args(" { 'name': 'HMAC', 'hash': 'SHA-256', 'length': 384 }") +
        "?k => bin:from-base64url() => bin:length()", 48);
    query("let $k := " + func.args(HS256) + " return " +
        verify.args("m", sign.args("m", " $k", HS256), " $k", HS256), true);
    query(func.args(HS256) + "?k = " + func.args(HS256) + "?k", false);

    error(func.args(" 'HMAC'"), CX_INVALGO_X);
    error(func.args(" { 'name': 'HMAC', 'length': 256 }"), CX_INVALGO_X);
    // length is in bits: 32 bits would be a 4-byte key
    error(func.args(" { 'name': 'HMAC', 'hash': 'SHA-256', 'length': 32 }"), CX_INVALGO_X);
    error(func.args(" { 'name': 'HMAC', 'hash': 'SHA-256', 'length': 260 }"), CX_INVALGO_X);
    error(func.args(" { 'name': 'ECDSA' }"), CX_INVALGO_X);
    error(func.args(" { 'name': 'ECDSA', 'named-curve': 'P-192' }"), CX_INVALGO_X);
    error(func.args(" { 'name': 'RSA-PSS', 'modulus-length': 100 }"), CX_INVALGO_X);
    // key generation cannot be interrupted: upper limits
    error(func.args(" { 'name': 'RSA-PSS', 'modulus-length': 65536 }"), CX_INVALGO_X);
    error(func.args(" { 'name': 'HMAC', 'hash': 'SHA-256', 'length': 8200 }"), CX_INVALGO_X);
    // randomized signatures are not merged
    query("let $k := " + func.args(" { 'name': 'ECDSA', 'named-curve': 'P-256' }") + " " +
        "return (1 to 2) ! " + _CRYPTO_SIGN.args("m", " $k?private", ES256) +
        " => distinct-values() => count()", 2);
    error(func.args(" { 'name': 'Ed25519', 'named-curve': 'P-256' }"), CX_INVALGO_X);
    error(func.args(" { 'name': 'HMAC', 'hash': 'SHA-256', 'x': 1 }"), INVALIDOPTION_X);
  }

  /** Tests the export of keys. */
  @Test public void exportKey() {
    final Function func = _CRYPTO_EXPORT_KEY, verify = _CRYPTO_VERIFY, sign = _CRYPTO_SIGN;
    final String edPub = " map:remove(" + ED_JWK + ", 'd')";

    // JWK round trips
    query(func.args(ED_JWK, "jwk") + " => map:keys()", "kty\ncrv\nx\nd");
    query("deep-equal(" + func.args(ED_JWK, "jwk") + ", " + ED_JWK + ")", true);
    query("deep-equal(" + func.args(EC_JWK, "jwk") + ", " + EC_JWK + ")", true);
    query("deep-equal(" + func.args(RSA_JWK, "jwk") + ", " + RSA_JWK + ")", true);
    query("deep-equal(" + func.args(func.args(RSA_JWK, "pem"), "jwk") + ", " + RSA_JWK + ")",
        true);
    query("deep-equal(" + func.args(func.args(EC_JWK, "pkcs8"), "jwk") + ", " + EC_JWK + ")",
        true);
    query(func.args("secret", "jwk") + " => serialize({ 'method': 'json' })",
        "{\"kty\":\"oct\",\"k\":\"c2VjcmV0\"}");

    // PEM, DER, raw
    query(func.args(ED_JWK, "pem") + " => starts-with('-----BEGIN PRIVATE KEY-----')", true);
    query(func.args(edPub, "pem") + " => starts-with('-----BEGIN PUBLIC KEY-----')", true);
    query(func.args(edPub, "raw") + " => bin:to-base64url()",
        "11qYAYKxCrfVS_7TyWQHOg7hcvPapiMlrwIaaPcHURo");
    query(func.args(EC_JWK, "raw") + " => bin:length()", 65);
    query(func.args("secret", "raw") + " => string()", "c2VjcmV0");
    query(verify.args("m", sign.args("m", func.args(RSA_JWK, "pkcs8"), RS256),
        func.args(RSA_JWK, "spki"), RS256), true);
    query(verify.args("m", sign.args("m", func.args(ED_JWK, "pem"), "Ed25519"),
        func.args(ED_JWK, "spki"), "Ed25519"), true);

    // SEC1 key with EC parameters, as written by "openssl ecparam -genkey"
    final String sec1 = "-----BEGIN EC PARAMETERS-----\nBggqhkjOPQMBBw==\n" +
        "-----END EC PARAMETERS-----\n-----BEGIN EC PRIVATE KEY-----\n" +
        "MHcCAQEEICllmUgPJhBWJlqeW1xrvyZzh/ENGFjPl17cW7OANsqRoAoGCCqGSM49\n" +
        "AwEHoUQDQgAEnv+p5E+X35KBLzjiy/VaoQi/RXv9LqI4nh3s26I8Jj6PHgxOkfeW\n" +
        "DWLx7FdEFnHvZy2V440cPh50dQ9BaKfo+Q==\n-----END EC PRIVATE KEY-----\n";
    final String sec1Pub = "-----BEGIN PUBLIC KEY-----\n" +
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEnv+p5E+X35KBLzjiy/VaoQi/RXv9\n" +
        "LqI4nh3s26I8Jj6PHgxOkfeWDWLx7FdEFnHvZy2V440cPh50dQ9BaKfo+Q==\n" +
        "-----END PUBLIC KEY-----\n";
    query(func.args(sec1, "jwk") + " => map:keys()", "kty\ncrv\nx\ny\nd");
    query(func.args(sec1, "spki") + " = " + func.args(sec1Pub, "spki"), true);
    query(verify.args("m", sign.args("m", sec1, ES256), sec1Pub, ES256), true);

    error(func.args("secret", "pem"), CX_EXPORT_X);
    error(func.args("secret", "spki"), CX_EXPORT_X);
    error(func.args(edPub, "pkcs8"), CX_EXPORT_X);
    error(func.args(RSA_JWK, "raw"), CX_EXPORT_X);
    // public key of Ed25519 private keys in PKCS #8 format cannot be derived
    error(func.args(func.args(ED_JWK, "pem"), "jwk"), CX_EXPORT_X);
    error(func.args(ED_JWK, "xyz"), EXP_FOUND_X_X);

    // binary items must be DER keys; corrupted keys are rejected instead of used as secrets
    error(func.args(" xs:hexBinary('0102030405')", "jwk"), CX_KEYINV_X);
    error(func.args(" bin:part(" + func.args(ED_JWK, "spki") + ", 0, 40)", "jwk"), CX_KEYINV_X);
    error(func.args("-----BEGIN PUBLC KEY-----\nAAAA\n-----END PUBLIC KEY-----", "jwk"),
        CX_KEYINV_X);
    error(func.args("-----BEGIN PUBLIC KEY-----\nAAAA\n", "jwk"), CX_KEYINV_X);

    // unsupported key types are rejected (X448 key, created with OpenSSL)
    error(func.args("-----BEGIN PUBLIC KEY-----\n" +
        "MEIwBQYDK2VvAzkAA4dvjZxvSjVVlMgXkzjxMX9w52p9yAlYO2hJoUF2yinC8GnP\n" +
        "VHSpH+QkUuxMesej4f5gmYdeesg=\n-----END PUBLIC KEY-----", "jwk"), CX_KEYINV_X);
    // DER element with a length that overflows the end offset: no internal error
    error(func.args("-----BEGIN EC PRIVATE KEY-----\nMAYChH////8=\n-----END EC PRIVATE KEY-----",
        "jwk"), CX_KEYINV_X);
    // child that exceeds its parent, truncated long-form length
    error(func.args("-----BEGIN EC PRIVATE KEY-----\nMAMCAgAAAAA=\n-----END EC PRIVATE KEY-----",
        "jwk"), CX_KEYINV_X);
    error(func.args("-----BEGIN EC PRIVATE KEY-----\nMIIA\n-----END EC PRIVATE KEY-----", "jwk"),
        CX_KEYINV_X);
    // SEC1 key with a malformed public point (1-byte BIT STRING): no internal error
    error(func.args("-----BEGIN EC PRIVATE KEY-----\n" + Base64.getEncoder().encodeToString(
        HexFormat.of().parseHex("3036020101" + "0420" + "11".repeat(32) +
        "a00a06082a8648ce3d030107a103030100")) + "\n-----END EC PRIVATE KEY-----", "jwk"),
        CX_EXPORT_X);
    query(func.args(" { 'kty': 'oct', 'k': bin:to-base64url(xs:hexBinary('0102030405')) }",
        "raw") + " => string()", "AQIDBAU=");
  }

  /** Tests JWK thumbprints (test vector from RFC 8037, Appendix A.3). */
  @Test public void thumbprint() {
    final Function func = _CRYPTO_THUMBPRINT;
    final String tp = "kPrK_qmxVWaYVA9wwBF6Iuo3vVzz7TxHCTwXBygrS4k";
    query(func.args(ED_JWK), tp);
    query(func.args(" map:remove(" + ED_JWK + ", 'd')"), tp);
    query(func.args(_CRYPTO_EXPORT_KEY.args(ED_JWK, "spki")), tp);
    query(func.args(RSA_JWK) + " = " + func.args(_CRYPTO_EXPORT_KEY.args(RSA_JWK, "pem")),
        true);
    query(func.args(" { 'kty': 'oct', 'k': 'c2VjcmV0' }") + " = " + func.args("secret"), true);
    error(func.args(" { 'kty': 'xyz' }"), CX_KEYINV_X);
  }

  /** Tests the creation of random bytes. */
  @Test public void randomBytes() {
    final Function func = _CRYPTO_RANDOM_BYTES;
    query(func.args(16) + " => bin:length()", 16);
    query(func.args(0) + " => string()", "");
    query(func.args(16) + " = " + func.args(16), false);
    error(func.args(-1), INVTYPE_X);
    error(func.args(3000000000L), RANGE_X);
  }

  /** Tests the constant-time comparison. */
  @Test public void equal() {
    final Function func = _CRYPTO_EQUAL;
    query(func.args("abc", "abc"), true);
    query(func.args("abc", "abd"), false);
    query(func.args("abc", "ab"), false);
    query(func.args("abc", " xs:hexBinary('616263')"), true);
    query(func.args("", " xs:base64Binary('')"), true);
  }

  /** Tests the hashing and verification of passwords (test vectors from RFC 7914, Node.js). */
  @Test public void password() {
    final Function func = _CRYPTO_HASH_PASSWORD, func2 = _CRYPTO_VERIFY_PASSWORD;

    // Argon2id vectors in PHC format (lanes, uneven memory size, empty password, long key)
    query(func2.args("password", "$argon2id$v=19$m=64,t=2,p=1$c29tZXNhbHQ$" +
        "FqGkmHNGCd0BRW2kBt6fPZ2pPmyGwwChL8FGUhTOSSI"), true);
    query(func2.args("password", "$argon2id$v=19$m=32,t=3,p=4$c29tZXNhbHQ$" +
        "uwzICj5nEUlSaRVBjG7v52G7GdXS1WegF3A+DOpqsFw"), true);
    query(func2.args("password", "$argon2id$v=19$m=37,t=1,p=2$c29tZXNhbHQ$" +
        "4l0kmG8ivD5t2ak/U1Ksow"), true);
    query(func2.args("", "$argon2id$v=19$m=8,t=1,p=1$c2FsdHNhbHQ$" +
        "yKPDauzIcAZMUK20FOm8ZhRh2MNJ+qVwk4FMs7XZszpV1jmp/sUSjqMW6i9y+CbKei069V8BkLHZJkxrlNB3xIFf" +
        "oJ94cJI2k3UMNBYaFHUOk4+YoxIrfFVFeTsB2HD7o5yMLQ"), true);
    query(func2.args("password", "$argon2id$v=19$m=19456,t=2,p=1$c29tZXNhbHQ$" +
        "PL01amPyeUuxG7H0vIr5X+qHkZvWnHmGBGXFYvh8z2E"), true);
    query(func2.args("Password", "$argon2id$v=19$m=64,t=2,p=1$c29tZXNhbHQ$" +
        "FqGkmHNGCd0BRW2kBt6fPZ2pPmyGwwChL8FGUhTOSSI"), false);

    // PBKDF2-HMAC-SHA256 vectors in PHC format
    query(func2.args("password", "$pbkdf2-sha256$i=1$c2FsdA$" +
        "Eg+2z/z4syxD5yJSVsT4N6hlSMkszDVICAWYfLcL4Xs"), true);
    query(func2.args("password", "$pbkdf2-sha256$i=4096$c2FsdA$" +
        "xeR41ZKIyEGqUw22hFxMjZYok6ABzk4RpJY4c6qYE0o"), true);
    query(func2.args("Password", "$pbkdf2-sha256$i=1$c2FsdA$" +
        "Eg+2z/z4syxD5yJSVsT4N6hlSMkszDVICAWYfLcL4Xs"), false);

    // round trips, random salts
    for(final String opts : new String[] { " { 'memory': 64 }",
        " { 'scheme': 'pbkdf2', 'iterations': 1000 }" }) {
      query("let $h := " + func.args("secret", opts) + " return " + func2.args("secret", " $h"),
          true);
      query("let $h := " + func.args("secret", opts) + " return " + func2.args("Secret", " $h"),
          false);
      query("let $h := " + func.args("", opts) + " return " + func2.args("", " $h"), true);
      query(func.args("secret", opts) + " = " + func.args("secret", opts), false);
    }
    query(func.args("secret") + " => starts-with('$argon2id$v=19$m=19456,t=2,p=1$')", true);
    query(func.args("secret", " { 'scheme': 'pbkdf2' }") +
        " => starts-with('$pbkdf2-sha256$i=600000$')", true);
    query(func.args("secret", " { 'memory': 64, 'parallelism': 2, 'iterations': 1 }") +
        " => starts-with('$argon2id$v=19$m=64,t=1,p=2$')", true);
    query(func.args("secret", " { 'memory': 8, 'length': 100 }") +
        " => tokenize('\\$') => foot() => string-length()", 134);
    query(func.args("secret",
        " { 'scheme': 'pbkdf2', 'hash': 'SHA-512', 'iterations': 1, 'length': 64 }") +
        " => tokenize('\\$') => foot() => string-length()", 86);

    error(func.args("secret", " { 'scheme': 'pbkdf2', 'hash': 'MD5' }"), CX_INVHASH_X);
    error(func.args("secret", " { 'scheme': 'pbkdf2', 'iterations': 0 }"), CX_INVALGO_X);
    error(func.args("secret", " { 'scheme': 'pbkdf2', 'salt-length': 0 }"), CX_INVALGO_X);
    error(func.args("secret", " { 'scheme': 'pbkdf2', 'memory': 64 }"), CX_INVALGO_X);
    error(func.args("secret", " { 'hash': 'SHA-512' }"), CX_INVALGO_X);
    error(func.args("secret", " { 'hash': 'SHA-256' }"), CX_INVALGO_X);
    error(func.args("secret", " { 'scheme': 'pbkdf2', 'parallelism': 1 }"), CX_INVALGO_X);
    error(func.args("secret", " { 'salt-length': 1025 }"), CX_INVALGO_X);
    error(func.args("secret", " { 'length': 1025 }"), CX_INVALGO_X);
    error(func.args("secret", " { 'iterations': 0 }"), CX_INVALGO_X);
    error(func.args("secret", " { 'salt-length': 7 }"), CX_INVALGO_X);
    error(func.args("secret", " { 'length': 3 }"), CX_INVALGO_X);
    error(func.args("secret", " { 'memory': 15, 'parallelism': 2 }"), CX_INVALGO_X);
    error(func.args("secret", " { 'memory': 2000000 }"), CX_INVALGO_X);
    error(func.args("secret", " { 'parallelism': 0 }"), CX_INVALGO_X);
    error(func.args("secret", " { 'scheme': 'scrypt' }"), INVALIDOPTIONVALUE_X);
    error(func.args("secret", " { 'x': 1 }"), INVALIDOPTION_X);
    error(func2.args("secret", "xyz"), CX_PWHASH_X);
    error(func2.args("secret", "$argon2id$v=16$m=64,t=2,p=1$c29tZXNhbHQ$AAAAAA"), CX_PWHASH_X);
    error(func2.args("secret", "$argon2id$v=19$t=2,m=64,p=1$c29tZXNhbHQ$AAAAAA"), CX_PWHASH_X);
    error(func2.args("secret", "$argon2id$v=19$m=9999999,t=2,p=1$c29tZXNhbHQ$AAAAAA"),
        CX_PWHASH_X);
    error(func2.args("secret", "$argon2id$v=19$m=64,t=2,p=1$c2FsdA$AAAAAA"), CX_PWHASH_X);
    error(func2.args("secret", "$argon2i$v=19$m=64,t=2,p=1$c29tZXNhbHQ$AAAAAA"), CX_PWHASH_X);
    error(func2.args("secret", "$pbkdf2-md5$i=1$c2FsdA$AAAA"), CX_PWHASH_X);
    error(func2.args("secret", "$pbkdf2-sha256$i=0$c2FsdA$AAAA"), CX_PWHASH_X);
    error(func2.args("secret", "$pbkdf2-sha256$i=1$***$AAAA"), CX_PWHASH_X);
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
    // only hash algorithms of the PBKDF2 implementation of the JDK
    error(func.args("password", "salt", 1, 16, "MD5"), CX_INVHASH_X);
    query(func.args("password", "salt", 1, 28, "SHA-224") + " => string()",
        "3C198CBDB9464B7857966BD05B7BC92BC1CC4E6E63155D4E490557FD");
    error(func.args("password", "", 1, 32), CX_KEYINV_X);
    error(func.args("password", "salt", 0, 32), INVTYPE_X);
    error(func.args("password", "salt", 1, 0), INVTYPE_X);
  }

  /**
   * Returns a binary literal for the encoded key.
   * @param key key
   * @return query string
   */
  private static String b64(final Key key) {
    return " xs:base64Binary('" + Base64.getEncoder().encodeToString(key.getEncoded()) + "')";
  }

  /**
   * Returns a PEM string.
   * @param label PEM label
   * @param der DER bytes
   * @return PEM string
   */
  private static String pem(final String label, final byte[] der) {
    return "-----BEGIN " + label + "-----\n" + Base64.getMimeEncoder().encodeToString(der) +
        "\n-----END " + label + "-----\n";
  }
}
