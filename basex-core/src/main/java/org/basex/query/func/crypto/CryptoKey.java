package org.basex.query.func.crypto;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.math.*;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.basex.query.*;
import org.basex.query.func.crypto.DerFormat.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Imports and exports cryptographic keys (PEM, DER, raw secrets, JWK).
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
    ED448("OKP", "EdDSA", EdECKey.class, "Ed448"),
    /** X25519 key. */
    X25519("OKP", "XDH", XECKey.class, "X25519");

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
      return clazz.isInstance(key) && (curve == null || curve.equalsIgnoreCase(curve(key)));
    }

    /**
     * Returns the type of a key.
     * @param key key
     * @return type (can be {@code null})
     */
    static Type of(final Key key) {
      for(final Type type : values()) {
        if(type.matches(key)) return type;
      }
      return null;
    }

    /**
     * Returns the curve name of an EdDSA or XDH key.
     * @param key key
     * @return curve name (can be {@code null})
     */
    static String curve(final Key key) {
      final AlgorithmParameterSpec params = key instanceof final EdECKey ed ? ed.getParams() :
        key instanceof final XECKey xec ? xec.getParams() : null;
      return params instanceof final NamedParameterSpec nps ? nps.getName() : null;
    }

    @Override
    public String toString() {
      return curve != null ? curve : kty;
    }
  }

  /** Key usages. */
  enum Usage {
    /** Signing. */
    SIGN(true, "sig", "sign"),
    /** Verification. */
    VERIFY(false, "sig", "verify"),
    /** Encryption. */
    ENCRYPT(false, "enc", "encrypt"),
    /** Decryption. */
    DECRYPT(true, "enc", "decrypt"),
    /** Key derivation (own key). */
    DERIVE(true, "enc", "deriveBits", "deriveKey"),
    /** Key agreement (public key of the other party). */
    PEER(false, "enc");

    /** Private key required. */
    final boolean priv;
    /** JWK use. */
    final String use;
    /** Accepted JWK key operations (empty: no check). */
    final List<String> ops;

    /**
     * Constructor.
     * @param priv private key required
     * @param use JWK use
     * @param ops accepted JWK key operations (empty: no check)
     */
    Usage(final boolean priv, final String use, final String... ops) {
      this.priv = priv;
      this.use = use;
      this.ops = List.of(ops);
    }
  }

  /** Export formats. */
  enum Format {
    /** JSON Web Key. */ JWK,
    /** PEM. */ PEM,
    /** DER: X.509 SubjectPublicKeyInfo. */ SPKI,
    /** DER: PKCS #8. */ PKCS8,
    /** Raw bytes. */ RAW;

    @Override
    public String toString() {
      return Enums.string(this);
    }
  }

  /**
   * Key material: secret key, or public and/or private key.
   * @param secret secret key (can be {@code null})
   * @param pub public key (can be {@code null})
   * @param priv private key (can be {@code null})
   */
  record Keys(SecretKey secret, PublicKey pub, PrivateKey priv) { }

  /** Input info (can be {@code null}). */
  final InputInfo info;
  /** JWK format. */
  private final JwkFormat jwk;
  /** PEM and DER formats. */
  private final DerFormat der;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   */
  CryptoKey(final InputInfo info) {
    this.info = info;
    jwk = new JwkFormat(this);
    der = new DerFormat(this);
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
      if(item instanceof final XQMap map) return jwk.read(map, type, usage, algs);
      final byte[] bytes = bytes(item, info);
      final Pem pem = der.pem(bytes, !type.secret());
      if(pem != null) {
        if(type.secret()) throw CX_KEYALG_X.get(info, "PEM key for secret-key algorithm");
        if(usage.priv != pem.priv()) throw usage(usage.priv);
        return der.read(pem, type);
      }
      if(type.secret()) return secret(bytes, type);
      if(item instanceof Bin) return der.read(bytes, type, usage.priv);
      throw CX_KEYINV_X.get(info, "PEM or DER key expected");
    } catch(final GeneralSecurityException | IllegalArgumentException ex) {
      throw CX_KEYINV_X.get(info, ex);
    }
  }

  /**
   * Imports a key of any type.
   * @param item key item (string, binary, JWK map)
   * @return key material
   * @throws QueryException query exception
   */
  Keys any(final Item item) throws QueryException {
    final Keys keys;
    try {
      if(item instanceof final XQMap map) {
        keys = jwk.read(map);
      } else {
        final byte[] bytes = bytes(item, info);
        final Pem pem = der.pem(bytes, true);
        // strings: secret keys; binary items: DER keys (secrets can be supplied as JWK)
        keys = pem != null ? der.read(pem) : item instanceof Bin ? der.read(bytes) :
          new Keys(secret(bytes, Type.HMAC), null, null);
      }
    } catch(final GeneralSecurityException | IllegalArgumentException ex) {
      throw CX_KEYINV_X.get(info, ex);
    }
    for(final Key key : new Key[] { keys.pub(), keys.priv() }) {
      if(key != null && Type.of(key) == null) {
        throw CX_KEYINV_X.get(info, "unsupported key type " + key.getAlgorithm());
      }
    }
    return keys;
  }

  /**
   * Exports a key.
   * @param keys key material
   * @param format export format
   * @return exported key
   * @throws QueryException query exception
   */
  Item export(final Keys keys, final Format format) throws QueryException {
    return switch(format) {
      case JWK -> jwk(keys);
      case PEM -> Str.get(der.pem(keys));
      case SPKI -> B64.get(der.spki(keys));
      case PKCS8 -> B64.get(der.pkcs8(keys));
      case RAW -> {
        // secret key, or public EC/OKP point
        if(keys.secret() != null) yield B64.get(keys.secret().getEncoded());
        final PublicKey pub = pub(keys);
        if(pub instanceof final ECPublicKey ec) yield B64.get(point(ec));
        if(pub instanceof EdECPublicKey || pub instanceof XECPublicKey) yield B64.get(okp(pub));
        throw CX_EXPORT_X.get(info, "RSA key in raw format");
      }
    };
  }

  /**
   * Exports a key as JWK.
   * @param keys key material
   * @return JWK map
   * @throws QueryException query exception
   */
  XQMap jwk(final Keys keys) throws QueryException {
    return jwk.write(keys);
  }

  /**
   * Returns the JWK thumbprint (RFC 7638, SHA-256).
   * @param keys key material
   * @return thumbprint (base64url-encoded)
   * @throws QueryException query exception
   */
  String thumbprint(final Keys keys) throws QueryException {
    return jwk.thumbprint(keys);
  }

  /**
   * Returns a secret key.
   * @param bytes raw bytes
   * @param type key type
   * @return key
   * @throws QueryException query exception
   */
  SecretKey secret(final byte[] bytes, final Type type) throws QueryException {
    if(bytes.length == 0) throw CX_KEYINV_X.get(info, "empty secret");
    return new SecretKeySpec(bytes, type.jca);
  }

  /**
   * Checks if the asymmetric key has the expected type.
   * @param key key
   * @param type key type
   * @return key
   * @throws QueryException query exception
   */
  Key check(final Key key, final Type type) throws QueryException {
    if(type.matches(key)) return key;
    final Type tp = Type.of(key);
    throw CX_KEYALG_X.get(info, tp != null ? tp + " key, " + type + " key expected" :
      key.getAlgorithm() + " key");
  }

  /**
   * Returns an error for a public key supplied as private key or vice versa.
   * @param priv private key expected
   * @return query exception
   */
  QueryException usage(final boolean priv) {
    return CX_KEYINV_X.get(info, (priv ? "private" : "public") + " key expected");
  }

  /**
   * Returns the public key of key material.
   * @param keys key material
   * @return public key
   * @throws QueryException query exception
   */
  PublicKey pub(final Keys keys) throws QueryException {
    if(keys.pub() != null) return keys.pub();
    // newer JDKs: check if public EdDSA/XDH keys can be derived from private keys (Java 21: no)
    throw CX_EXPORT_X.get(info, keys.secret() != null ? "secret key" :
      "public key cannot be derived from private key");
  }

  /**
   * Returns the bytes of a string or binary item.
   * @param item item
   * @param info input info (can be {@code null})
   * @return bytes
   * @throws QueryException query exception
   */
  static byte[] bytes(final Item item, final InputInfo info) throws QueryException {
    return item instanceof final Bin bin ? bin.binary(info) : item.string(info);
  }

  /**
   * Returns the uncompressed public point of an EC key (04, x, y).
   * @param key public key
   * @return encoded point
   */
  static byte[] point(final ECPublicKey key) {
    final int l = size(key.getParams());
    return concat(new byte[] { 4 }, unsigned(key.getW().getAffineX(), l),
        unsigned(key.getW().getAffineY(), l));
  }

  /**
   * Returns the encoded public key of an OKP key.
   * @param key public key
   * @return encoded key (EdDSA: little-endian y, sign bit of x; XDH: little-endian u)
   */
  static byte[] okp(final PublicKey key) {
    if(key instanceof final XECPublicKey xec) return reverse(unsigned(xec.getU(), 32));
    final EdECPublicKey ed = (EdECPublicKey) key;
    final int l = Type.ED25519.matches(ed) ? 32 : 57;
    final byte[] x = reverse(unsigned(ed.getPoint().getY(), l));
    if(ed.getPoint().isXOdd()) x[l - 1] |= (byte) 0x80;
    return x;
  }

  /**
   * Returns the byte length of the coordinates of an EC curve.
   * @param params curve parameters
   * @return length
   */
  static int size(final ECParameterSpec params) {
    return (params.getCurve().getField().getFieldSize() + 7) / 8;
  }

  /**
   * Returns the unsigned big-endian bytes of an integer, padded to the specified length.
   * @param value integer
   * @param length minimum length
   * @return bytes
   */
  static byte[] unsigned(final BigInteger value, final int length) {
    final byte[] bytes = value.toByteArray();
    final int s = bytes.length > 1 && bytes[0] == 0 ? 1 : 0, l = bytes.length - s;
    final byte[] result = new byte[Math.max(l, length)];
    System.arraycopy(bytes, s, result, result.length - l, l);
    return result;
  }

  /**
   * Returns a reversed copy of the specified bytes.
   * @param bytes bytes
   * @return reversed bytes
   */
  static byte[] reverse(final byte[] bytes) {
    final int l = bytes.length;
    final byte[] reversed = new byte[l];
    for(int i = 0; i < l; i++) reversed[i] = bytes[l - 1 - i];
    return reversed;
  }
}
