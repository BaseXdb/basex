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
    error(func2.args(input, sig, pub, " { 'name': 'Ed25519', 'hash': 'SHA-256' }"), CX_INVALGO_X);
    error(func2.args(input, sig, " map:put(" + pub + ", 'crv', 'X25519')", "Ed25519"),
        CX_KEYALG_X);
    error(func2.args(input, sig, " map:put(" + pub + ", 'kty', 'EC')", "Ed25519"), CX_KEYALG_X);
    error(func.args(input, pub, "Ed25519"), CX_KEYINV_X);
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
