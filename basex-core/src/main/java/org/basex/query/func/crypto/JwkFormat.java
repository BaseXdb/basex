package org.basex.query.func.crypto;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.math.*;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import java.util.Base64;
import java.util.Map.*;

import org.basex.query.*;
import org.basex.query.func.crypto.CryptoAlgorithm.*;
import org.basex.query.func.crypto.CryptoKey.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Reads and writes keys in the JSON Web Key format (RFC 7517, RFC 7518, RFC 8037).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class JwkFormat {
  /** Named EC curves (JWK name, JCA name). */
  static final Map<String, String> CURVES = Map.of(
      "P-256", "secp256r1", "P-384", "secp384r1", "P-521", "secp521r1");
  /** Base64url encoder (without padding). */
  private static final Base64.Encoder BASE64URL = Base64.getUrlEncoder().withoutPadding();

  /** Key facade. */
  private final CryptoKey ck;
  /** Input info (can be {@code null}). */
  private final InputInfo info;

  /**
   * Constructor.
   * @param ck key facade
   */
  JwkFormat(final CryptoKey ck) {
    this.ck = ck;
    info = ck.info;
  }

  /**
   * Reads a key for the specified type and usage.
   * @param jwk JWK map
   * @param type key type
   * @param usage key usage
   * @param algs compatible JWK algorithm names (can be {@code null})
   * @return key
   * @throws QueryException query exception
   * @throws GeneralSecurityException security exception
   */
  Key read(final XQMap jwk, final Type type, final Usage usage, final List<String> algs)
      throws QueryException, GeneralSecurityException {

    final String kty = member(jwk, "kty", true);
    if(!kty.equals(type.kty)) throw CX_KEYALG_X.get(info, "JWK key type '" + kty + "'");
    final String use = member(jwk, "use", false);
    if(use != null && !use.equals(usage.use)) {
      throw CX_KEYALG_X.get(info, "JWK use '" + use + "'");
    }
    final Value ops = jwk.getOrNull(Str.get("key_ops"));
    if(ops != null && !usage.ops.isEmpty() && !includes(ops, usage.ops)) {
      throw CX_KEYALG_X.get(info, "JWK key_ops without " + usage.ops);
    }
    final String a = member(jwk, "alg", false);
    if(a != null && algs != null && !algs.contains(a)) {
      throw CX_KEYALG_X.get(info, "JWK algorithm '" + a + "'");
    }

    if(type.secret()) return ck.secret(decode(jwk, "k", true), type);
    if(!usage.priv) return publicKey(jwk, type);
    final byte[] d = decode(jwk, "d", false);
    if(d == null) throw ck.usage(true);
    return privateKey(jwk, type, d);
  }

  /**
   * Reads a key of any type.
   * @param jwk JWK map
   * @return key material
   * @throws QueryException query exception
   * @throws GeneralSecurityException security exception
   */
  Keys read(final XQMap jwk) throws QueryException, GeneralSecurityException {
    final String kty = member(jwk, "kty", true);
    if(kty.equals("oct")) return new Keys(ck.secret(decode(jwk, "k", true), Type.HMAC), null, null);

    Type type = null;
    for(final Type tp : Type.values()) {
      if(!tp.secret() && tp.kty.equals(kty) &&
          (tp.curve == null || tp.curve.equals(member(jwk, "crv", true)))) {
        type = tp;
        break;
      }
    }
    if(type == null) throw CX_KEYINV_X.get(info, "unsupported JWK key type '" + kty + "'");
    final byte[] d = decode(jwk, "d", false);
    return new Keys(null, publicKey(jwk, type), d != null ? privateKey(jwk, type, d) : null);
  }

  /**
   * Writes a key.
   * @param keys key material
   * @return JWK map
   * @throws QueryException query exception
   */
  XQMap write(final Keys keys) throws QueryException {
    final MapBuilder mb = new MapBuilder();
    for(final Entry<String, String> entry : members(keys, false).entrySet()) {
      mb.put(entry.getKey(), entry.getValue());
    }
    return mb.map();
  }

  /**
   * Returns the JWK thumbprint (RFC 7638, SHA-256).
   * @param keys key material
   * @return thumbprint (base64url-encoded)
   * @throws QueryException query exception
   */
  String thumbprint(final Keys keys) throws QueryException {
    // required members in lexicographic order, without whitespace
    final TreeMap<String, String> members = new TreeMap<>(members(keys, true));
    final StringJoiner json = new StringJoiner(",", "{", "}");
    members.forEach((name, value) -> json.add('"' + name + "\":\"" + value + '"'));
    try {
      final MessageDigest md = MessageDigest.getInstance(Hash.SHA_256.jca);
      return base64url(md.digest(token(json.toString())));
    } catch(final NoSuchAlgorithmException ex) {
      throw Util.notExpected(ex);
    }
  }

  /**
   * Returns the members of a JWK.
   * @param keys key material
   * @param required only return members required for the public key
   * @return members
   * @throws QueryException query exception
   */
  private Map<String, String> members(final Keys keys, final boolean required)
      throws QueryException {
    final Map<String, String> map = new LinkedHashMap<>();
    if(keys.secret() != null) {
      map.put("kty", "oct");
      map.put("k", base64url(keys.secret().getEncoded()));
      return map;
    }
    final PrivateKey priv = required ? null : keys.priv();
    final PublicKey pub = ck.pub(keys);
    if(pub instanceof final RSAPublicKey rsa) {
      map.put("kty", "RSA");
      map.put("n", base64url(rsa.getModulus()));
      map.put("e", base64url(rsa.getPublicExponent()));
    } else if(pub instanceof final ECPublicKey ec) {
      final int l = CryptoKey.size(ec.getParams());
      map.put("kty", "EC");
      map.put("crv", curve(ec.getParams()));
      map.put("x", base64url(CryptoKey.unsigned(ec.getW().getAffineX(), l)));
      map.put("y", base64url(CryptoKey.unsigned(ec.getW().getAffineY(), l)));
    } else if(pub instanceof EdECPublicKey || pub instanceof XECPublicKey) {
      map.put("kty", "OKP");
      map.put("crv", Type.curve(pub));
      map.put("x", base64url(CryptoKey.okp(pub)));
    }
    if(priv instanceof final RSAPrivateCrtKey rsa) {
      map.put("d", base64url(rsa.getPrivateExponent()));
      map.put("p", base64url(rsa.getPrimeP()));
      map.put("q", base64url(rsa.getPrimeQ()));
      map.put("dp", base64url(rsa.getPrimeExponentP()));
      map.put("dq", base64url(rsa.getPrimeExponentQ()));
      map.put("qi", base64url(rsa.getCrtCoefficient()));
    } else if(priv instanceof final ECPrivateKey ec) {
      map.put("d", base64url(CryptoKey.unsigned(ec.getS(), CryptoKey.size(ec.getParams()))));
    } else if(priv instanceof final EdECPrivateKey ed) {
      map.put("d", base64url(ed.getBytes().orElseThrow()));
    } else if(priv instanceof final XECPrivateKey xec) {
      map.put("d", base64url(xec.getScalar().orElseThrow()));
    } else if(priv != null) {
      throw CX_EXPORT_X.get(info, priv.getAlgorithm() + " private key");
    }
    return map;
  }

  /**
   * Creates the public key of an asymmetric JWK.
   * @param jwk JWK map
   * @param type key type
   * @return key
   * @throws QueryException query exception
   * @throws GeneralSecurityException security exception
   */
  private PublicKey publicKey(final XQMap jwk, final Type type)
      throws QueryException, GeneralSecurityException {
    final KeyFactory kf = KeyFactory.getInstance(type.jca);
    return switch(type) {
      case RSA -> kf.generatePublic(new RSAPublicKeySpec(bigint(jwk, "n"), bigint(jwk, "e")));
      case EC -> kf.generatePublic(new ECPublicKeySpec(
          new ECPoint(bigint(jwk, "x"), bigint(jwk, "y")), curve(jwk)));
      default -> {
        // check the curve first (mismatches are reported as such)
        final NamedParameterSpec spec = okp(jwk, type);
        // little-endian coordinate; most significant bit: odd x (EdDSA), masked (X25519)
        final byte[] y = CryptoKey.reverse(decode(jwk, "x", true));
        if(y.length != (type == Type.ED448 ? 57 : 32)) {
          throw CX_KEYINV_X.get(info, "JWK member 'x' has an invalid length");
        }
        final boolean odd = (y[0] & 0x80) != 0;
        if(odd) y[0] &= 0x7F;
        final BigInteger value = new BigInteger(1, y);
        yield kf.generatePublic(type == Type.X25519 ? new XECPublicKeySpec(spec, value) :
          new EdECPublicKeySpec(spec, new EdECPoint(odd, value)));
      }
    };
  }

  /**
   * Creates the private key of an asymmetric JWK.
   * @param jwk JWK map
   * @param type key type
   * @param d private key bytes
   * @return key
   * @throws QueryException query exception
   * @throws GeneralSecurityException security exception
   */
  private PrivateKey privateKey(final XQMap jwk, final Type type, final byte[] d)
      throws QueryException, GeneralSecurityException {
    final KeyFactory kf = KeyFactory.getInstance(type.jca);
    return switch(type) {
      case RSA -> {
        final BigInteger n = bigint(jwk, "n"), dd = new BigInteger(1, d);
        final byte[] p = decode(jwk, "p", false);
        yield kf.generatePrivate(p == null ? new RSAPrivateKeySpec(n, dd) :
          new RSAPrivateCrtKeySpec(n, bigint(jwk, "e"), dd, new BigInteger(1, p),
            bigint(jwk, "q"), bigint(jwk, "dp"), bigint(jwk, "dq"), bigint(jwk, "qi")));
      }
      case EC -> kf.generatePrivate(new ECPrivateKeySpec(new BigInteger(1, d), curve(jwk)));
      case X25519 -> kf.generatePrivate(new XECPrivateKeySpec(okp(jwk, type), d));
      default -> kf.generatePrivate(new EdECPrivateKeySpec(okp(jwk, type), d));
    };
  }

  /**
   * Returns the parameters of an EC curve.
   * @param jwk JWK map
   * @return parameters
   * @throws QueryException query exception
   * @throws GeneralSecurityException security exception
   */
  private ECParameterSpec curve(final XQMap jwk) throws QueryException, GeneralSecurityException {
    final String crv = member(jwk, "crv", true), name = CURVES.get(crv);
    if(name == null) throw CX_KEYINV_X.get(info, "unsupported curve '" + crv + "'");
    return curve(name);
  }

  /**
   * Returns the JWK name of an EC curve.
   * @param params curve parameters
   * @return name
   * @throws QueryException query exception
   */
  private String curve(final ECParameterSpec params) throws QueryException {
    // the coordinate size identifies the candidate: P-256, P-384, P-521
    final int size = CryptoKey.size(params);
    final String name = size == 32 ? "P-256" : size == 48 ? "P-384" : size == 66 ? "P-521" : null;
    try {
      if(name != null) {
        final ECParameterSpec spec = curve(CURVES.get(name));
        if(spec.getCurve().equals(params.getCurve()) && spec.getOrder().equals(params.getOrder())) {
          return name;
        }
      }
    } catch(final GeneralSecurityException ex) {
      Util.debug(ex);
    }
    throw CX_EXPORT_X.get(info, "unsupported EC curve");
  }

  /**
   * Returns the parameters of a named EC curve.
   * @param name JCA name of the curve
   * @return parameters
   * @throws GeneralSecurityException security exception
   */
  private static ECParameterSpec curve(final String name) throws GeneralSecurityException {
    final AlgorithmParameters ap = AlgorithmParameters.getInstance("EC");
    ap.init(new ECGenParameterSpec(name));
    return ap.getParameterSpec(ECParameterSpec.class);
  }

  /**
   * Returns the parameters of an OKP curve.
   * @param jwk JWK map
   * @param type key type
   * @return parameters
   * @throws QueryException query exception
   */
  private NamedParameterSpec okp(final XQMap jwk, final Type type) throws QueryException {
    final String crv = member(jwk, "crv", true);
    if(!crv.equals(type.curve)) throw CX_KEYALG_X.get(info, "JWK curve '" + crv + "'");
    return new NamedParameterSpec(crv);
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
      return Base64.getUrlDecoder().decode(string);
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
   * Checks if a sequence or array contains one of the specified strings.
   * @param value sequence or array
   * @param strings strings
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean includes(final Value value, final List<String> strings) throws QueryException {
    for(final Item item : value) {
      if(item instanceof final XQArray array) {
        for(final Value member : array.members()) {
          if(includes(member, strings)) return true;
        }
      } else if(item.type.isStringOrUntyped() && strings.contains(string(item.string(info)))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the base64url encoding of an unsigned integer.
   * @param value integer
   * @return encoded string
   */
  private static String base64url(final BigInteger value) {
    return base64url(CryptoKey.unsigned(value, 0));
  }

  /**
   * Returns the base64url encoding of bytes (without padding).
   * @param bytes bytes
   * @return encoded string
   */
  private static String base64url(final byte[] bytes) {
    return BASE64URL.encodeToString(bytes);
  }
}
