package org.basex.query.func.crypto;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.math.*;
import java.security.*;
import java.security.cert.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import java.util.regex.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;
import org.basex.util.Base64;

/**
 * Imports cryptographic keys (PEM, DER, raw secrets, JWK).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class CryptoKey {
  /** Key types. */
  enum Type {
    /** HMAC secret. */
    HMAC("oct", "HMAC", SecretKey.class, null),
    /** AES secret. */
    AES("oct", "AES", SecretKey.class, null),
    /** RSA key. */
    RSA("RSA", "RSA", RSAKey.class, null),
    /** EC key. */
    EC("EC", "EC", ECKey.class, null),
    /** Ed25519 key. */
    ED25519("OKP", "EdDSA", EdECKey.class, "Ed25519"),
    /** Ed448 key. */
    ED448("OKP", "EdDSA", EdECKey.class, "Ed448");

    /** JWK key type. */
    final String kty;
    /** JCA name of the secret key or key factory. */
    final String jca;
    /** Key class. */
    final Class<?> clazz;
    /** Curve name (can be {@code null}). */
    final String curve;

    /**
     * Constructor.
     * @param kty JWK key type
     * @param jca JCA name of the secret key or key factory
     * @param clazz key class
     * @param curve curve name (can be {@code null})
     */
    Type(final String kty, final String jca, final Class<?> clazz, final String curve) {
      this.kty = kty;
      this.jca = jca;
      this.clazz = clazz;
      this.curve = curve;
    }

    /**
     * Checks if this is a secret key type.
     * @return result of check
     */
    boolean secret() {
      return kty.equals("oct");
    }

    /**
     * Checks if the specified key has this type.
     * @param key key
     * @return result of check
     */
    boolean matches(final Key key) {
      return clazz.isInstance(key) && (curve == null ||
        ((EdECKey) key).getParams().getName().equalsIgnoreCase(curve));
    }

    @Override
    public String toString() {
      return curve != null ? curve : kty;
    }
  }

  /** Key usages. */
  enum Usage {
    /** Signing. */
    SIGN(true, "sig"),
    /** Verification. */
    VERIFY(false, "sig"),
    /** Encryption. */
    ENCRYPT(false, "enc"),
    /** Decryption. */
    DECRYPT(true, "enc");

    /** Private key required. */
    final boolean priv;
    /** JWK use. */
    final String use;

    /**
     * Constructor.
     * @param priv private key required
     * @param use JWK use
     */
    Usage(final boolean priv, final String use) {
      this.priv = priv;
      this.use = use;
    }

    @Override
    public String toString() {
      return Enums.string(this);
    }
  }

  /** Start of a PEM block. */
  private static final byte[] PEM_START = token("-----BEGIN");
  /** DER algorithm identifier of RSA keys (OID 1.2.840.113549.1.1.1, no parameters). */
  private static final byte[] RSA_ALG = { 0x30, 0x0D, 0x06, 0x09, 0x2A, (byte) 0x86, 0x48,
    (byte) 0x86, (byte) 0xF7, 0x0D, 0x01, 0x01, 0x01, 0x05, 0x00 };
  /** PEM block (possessive base64 body: linear runtime for unterminated blocks). */
  private static final Pattern PEM = Pattern.compile(
      "-----BEGIN ([A-Z0-9 ]+)-----([A-Za-z0-9+/=\\s]*+)-----END \\1-----");
  /** Named EC curves (JWK name, JCA name). */
  private static final Map<String, String> CURVES = Map.of(
      "P-256", "secp256r1", "P-384", "secp384r1", "P-521", "secp521r1");

  /** Input info (can be {@code null}). */
  private final InputInfo info;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   */
  CryptoKey(final InputInfo info) {
    this.info = info;
  }

  /**
   * Imports a key.
   * @param item key item (string, binary, JWK map)
   * @param type key type
   * @param usage key usage
   * @param algs compatible JWK algorithm names (can be {@code null})
   * @return key
   * @throws QueryException query exception
   */
  Key get(final Item item, final Type type, final Usage usage, final List<String> algs)
      throws QueryException {
    try {
      if(item instanceof final XQMap jwk) return jwk(jwk, type, usage, algs);
      final byte[] bytes = item instanceof final Bin bin ? bin.binary(info) : item.string(info);
      final Matcher pem = contains(bytes, PEM_START) ? PEM.matcher(string(bytes)) : null;
      if(pem != null && pem.find()) {
        if(type.secret()) throw CX_KEYALG_X.get(info, "PEM key for secret-key algorithm");
        return pem(pem, type, usage.priv);
      }
      if(type.secret()) return secret(bytes, type);
      if(item instanceof Bin) return der(bytes, type, usage.priv);
      throw CX_KEYINV_X.get(info, "PEM or DER key expected");
    } catch(final GeneralSecurityException | IllegalArgumentException ex) {
      throw CX_KEYINV_X.get(info, ex);
    }
  }

  /**
   * Returns a secret key.
   * @param bytes raw bytes
   * @param type key type
   * @return key
   * @throws QueryException query exception
   */
  private Key secret(final byte[] bytes, final Type type) throws QueryException {
    if(bytes.length == 0) throw CX_KEYINV_X.get(info, "empty secret");
    return new SecretKeySpec(bytes, type.jca);
  }

  /**
   * Parses a key in PEM format.
   * @param pem matcher of the PEM block
   * @param type key type
   * @param priv private key
   * @return key
   * @throws QueryException query exception
   * @throws GeneralSecurityException security exception
   */
  private Key pem(final Matcher pem, final Type type, final boolean priv)
      throws QueryException, GeneralSecurityException {

    final String label = pem.group(1);
    final byte[] der = Base64.decode(token(pem.group(2)));
    switch(label) {
      case "PUBLIC KEY", "PRIVATE KEY", "RSA PUBLIC KEY", "RSA PRIVATE KEY":
        if(priv != label.endsWith("PRIVATE KEY")) throw usage(priv);
        return der(label.startsWith("RSA ") ? pkcs1(der, priv) : der, type, priv);
      case "CERTIFICATE":
        if(priv) throw usage(true);
        return certificate(der, type);
      default:
        throw CX_KEYINV_X.get(info, "unsupported PEM type '" + label + "'");
    }
  }

  /**
   * Wraps a PKCS #1 RSA key into an SPKI or PKCS #8 structure.
   * @param der DER bytes
   * @param priv private key
   * @return DER bytes
   */
  private static byte[] pkcs1(final byte[] der, final boolean priv) {
    return tlv(0x30, priv ? concat(new byte[] { 2, 1, 0 }, RSA_ALG, tlv(0x04, der)) :
      concat(RSA_ALG, tlv(0x03, concat(new byte[] { 0 }, der))));
  }

  /**
   * Encodes a DER element.
   * @param tag tag
   * @param content content
   * @return DER bytes
   */
  private static byte[] tlv(final int tag, final byte[] content) {
    int l = content.length;
    final int n = l < 0x80 ? 0 : (39 - Integer.numberOfLeadingZeros(l)) / 8;
    final byte[] head = new byte[n + 2];
    head[0] = (byte) tag;
    head[1] = (byte) (n == 0 ? l : 0x80 | n);
    for(int i = n + 1; i > 1; i--, l >>>= 8) head[i] = (byte) l;
    return concat(head, content);
  }

  /**
   * Parses a key in DER format (SPKI, PKCS#8 or X.509 certificate).
   * @param der DER bytes
   * @param type key type
   * @param priv private key
   * @return key
   * @throws QueryException query exception
   * @throws GeneralSecurityException security exception
   */
  private Key der(final byte[] der, final Type type, final boolean priv)
      throws QueryException, GeneralSecurityException {
    try {
      return check(generate(type.jca, der, priv), type);
    } catch(final InvalidKeySpecException ex) {
      if(!priv) {
        try {
          return certificate(der, type);
        } catch(final CertificateException e) {
          Util.debug(e);
        }
      }
      // other key types: report mismatches
      for(final Type tp : Type.values()) {
        if(tp.secret() || tp.jca.equals(type.jca)) continue;
        try {
          return check(generate(tp.jca, der, priv), type);
        } catch(final InvalidKeySpecException e) {
          Util.debug(e);
        }
      }
      throw ex;
    }
  }

  /**
   * Generates a public or private key from DER bytes.
   * @param jca JCA name of the key factory
   * @param der DER bytes
   * @param priv private key
   * @return key
   * @throws GeneralSecurityException security exception
   */
  private static Key generate(final String jca, final byte[] der, final boolean priv)
      throws GeneralSecurityException {
    final KeyFactory kf = KeyFactory.getInstance(jca);
    return priv ? kf.generatePrivate(new PKCS8EncodedKeySpec(der)) :
      kf.generatePublic(new X509EncodedKeySpec(der));
  }

  /**
   * Extracts the public key from an X.509 certificate.
   * @param der DER bytes
   * @param type key type
   * @return key
   * @throws QueryException query exception
   * @throws CertificateException certificate exception
   */
  private Key certificate(final byte[] der, final Type type)
      throws QueryException, CertificateException {
    return check(CertificateFactory.getInstance("X.509").generateCertificate(
        new ByteArrayInputStream(der)).getPublicKey(), type);
  }

  /**
   * Parses a key in JWK format.
   * @param jwk JWK map
   * @param type key type
   * @param usage key usage
   * @param algs compatible JWK algorithm names (can be {@code null})
   * @return key
   * @throws QueryException query exception
   * @throws GeneralSecurityException security exception
   */
  private Key jwk(final XQMap jwk, final Type type, final Usage usage,
      final List<String> algs) throws QueryException, GeneralSecurityException {

    final String kty = member(jwk, "kty", true);
    if(!kty.equals(type.kty)) throw CX_KEYALG_X.get(info, "JWK key type '" + kty + "'");
    final String use = member(jwk, "use", false);
    if(use != null && !use.equals(usage.use)) {
      throw CX_KEYALG_X.get(info, "JWK use '" + use + "'");
    }
    final Value ops = jwk.getOrNull(Str.get("key_ops"));
    if(ops != null && !includes(ops, usage.toString())) {
      throw CX_KEYALG_X.get(info, "JWK key_ops without '" + usage + "'");
    }
    final String a = member(jwk, "alg", false);
    if(a != null && algs != null && !algs.contains(a)) {
      throw CX_KEYALG_X.get(info, "JWK algorithm '" + a + "'");
    }

    final boolean priv = usage.priv;
    if(type.secret()) return secret(decode(jwk, "k", true), type);
    final byte[] d = decode(jwk, "d", false);
    if(priv && d == null) throw usage(true);

    final KeyFactory kf = KeyFactory.getInstance(type.jca);
    return switch(type) {
      case RSA -> {
        final BigInteger n = bigint(jwk, "n"), e = bigint(jwk, "e");
        if(!priv) yield kf.generatePublic(new RSAPublicKeySpec(n, e));
        final BigInteger dd = new BigInteger(1, d);
        final byte[] p = decode(jwk, "p", false);
        yield kf.generatePrivate(p == null ? new RSAPrivateKeySpec(n, dd) :
          new RSAPrivateCrtKeySpec(n, e, dd, new BigInteger(1, p), bigint(jwk, "q"),
            bigint(jwk, "dp"), bigint(jwk, "dq"), bigint(jwk, "qi")));
      }
      case EC -> {
        final String crv = member(jwk, "crv", true), name = CURVES.get(crv);
        if(name == null) throw CX_KEYINV_X.get(info, "unsupported curve '" + crv + "'");
        final AlgorithmParameters ap = AlgorithmParameters.getInstance("EC");
        ap.init(new ECGenParameterSpec(name));
        final ECParameterSpec spec = ap.getParameterSpec(ECParameterSpec.class);
        yield priv ? kf.generatePrivate(new ECPrivateKeySpec(new BigInteger(1, d), spec)) :
          kf.generatePublic(new ECPublicKeySpec(
              new ECPoint(bigint(jwk, "x"), bigint(jwk, "y")), spec));
      }
      default -> {
        final String crv = member(jwk, "crv", true);
        if(!crv.equals(type.curve)) throw CX_KEYALG_X.get(info, "JWK curve '" + crv + "'");
        final NamedParameterSpec spec = new NamedParameterSpec(crv);
        if(priv) yield kf.generatePrivate(new EdECPrivateKeySpec(spec, d));
        // little-endian y coordinate, most significant bit: x is odd
        final byte[] x = decode(jwk, "x", true);
        final int l = x.length;
        final boolean odd = l > 0 && (x[l - 1] & 0x80) != 0;
        final byte[] y = new byte[l];
        for(int i = 0; i < l; i++) y[i] = x[l - 1 - i];
        if(l > 0) y[0] &= 0x7F;
        yield kf.generatePublic(new EdECPublicKeySpec(spec, new EdECPoint(odd,
            new BigInteger(1, y))));
      }
    };
  }

  /**
   * Returns a string member of a JWK.
   * @param jwk JWK map
   * @param name name of the member
   * @param mandatory mandatory member
   * @return string (can be {@code null})
   * @throws QueryException query exception
   */
  private String member(final XQMap jwk, final String name, final boolean mandatory)
      throws QueryException {
    final Value value = jwk.getOrNull(Str.get(name));
    if(value == null) {
      if(mandatory) throw CX_KEYINV_X.get(info, "JWK member '" + name + "' is missing");
      return null;
    }
    if(!(value instanceof final Item item && item.type.isStringOrUntyped())) {
      throw CX_KEYINV_X.get(info, "JWK member '" + name + "' is no string");
    }
    return string(item.string(info));
  }

  /**
   * Returns a base64url-encoded member of a JWK.
   * @param jwk JWK map
   * @param name name of the member
   * @param mandatory mandatory member
   * @return bytes (can be {@code null})
   * @throws QueryException query exception
   */
  private byte[] decode(final XQMap jwk, final String name, final boolean mandatory)
      throws QueryException {
    final String string = member(jwk, name, mandatory);
    if(string == null) return null;
    try {
      return java.util.Base64.getUrlDecoder().decode(string);
    } catch(final IllegalArgumentException ex) {
      Util.debug(ex);
      throw CX_KEYINV_X.get(info, "JWK member '" + name + "' is no base64url string");
    }
  }

  /**
   * Returns a mandatory base64url-encoded member of a JWK as unsigned integer.
   * @param jwk JWK map
   * @param name name of the member
   * @return integer
   * @throws QueryException query exception
   */
  private BigInteger bigint(final XQMap jwk, final String name) throws QueryException {
    return new BigInteger(1, decode(jwk, name, true));
  }

  /**
   * Checks if the asymmetric key has the expected type.
   * @param key key
   * @param type key type
   * @return key
   * @throws QueryException query exception
   */
  private Key check(final Key key, final Type type) throws QueryException {
    if(type.matches(key)) return key;
    for(final Type tp : Type.values()) {
      if(tp.matches(key)) throw CX_KEYALG_X.get(info, tp + " key, " + type + " key expected");
    }
    throw CX_KEYALG_X.get(info, key.getAlgorithm() + " key");
  }

  /**
   * Checks if a sequence or array contains the specified string.
   * @param value sequence or array
   * @param string string
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean includes(final Value value, final String string) throws QueryException {
    for(final Item item : value) {
      if(item instanceof final XQArray array) {
        for(final Value member : array.members()) {
          if(includes(member, string)) return true;
        }
      } else if(item.type.isStringOrUntyped() && eq(item.string(info), token(string))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns an error for a public key supplied as private key or vice versa.
   * @param priv private key expected
   * @return query exception
   */
  private QueryException usage(final boolean priv) {
    return CX_KEYINV_X.get(info, (priv ? "private" : "public") + " key expected");
  }
}
