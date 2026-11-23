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

import org.basex.query.*;
import org.basex.query.func.crypto.CryptoKey.*;
import org.basex.util.*;
import org.basex.util.Base64;

/**
 * Reads and writes keys in the PEM and DER formats (SPKI, PKCS #8, PKCS #1, SEC1, X.509).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class DerFormat {
  /**
   * PEM block, normalized to SPKI, PKCS #8 or X.509 DER.
   * @param der DER bytes
   * @param priv private key
   * @param cert certificate
   */
  record Pem(byte[] der, boolean priv, boolean cert) { }

  /** Start of a PEM block. */
  private static final byte[] PEM_START = token("-----BEGIN");
  /** DER algorithm identifier of RSA keys (OID 1.2.840.113549.1.1.1, no parameters). */
  private static final byte[] RSA_ALG = { 0x30, 0x0D, 0x06, 0x09, 0x2A, (byte) 0x86, 0x48,
    (byte) 0x86, (byte) 0xF7, 0x0D, 0x01, 0x01, 0x01, 0x05, 0x00 };
  /** DER object identifier of EC keys (OID 1.2.840.10045.2.1). */
  private static final byte[] EC_OID = { 0x06, 0x07, 0x2A, (byte) 0x86, 0x48, (byte) 0xCE, 0x3D,
    0x02, 0x01 };
  /** PEM block (possessive base64 body: linear runtime for unterminated blocks). */
  private static final Pattern PEM = Pattern.compile(
      "-----BEGIN ([A-Z0-9 ]+)-----([A-Za-z0-9+/=\\s]*+)-----END \\1-----");

  /** MIME encoder for PEM bodies. */
  private static final java.util.Base64.Encoder MIME =
      java.util.Base64.getMimeEncoder(64, token("\n"));

  /** Key facade. */
  private final CryptoKey ck;
  /** Input info (can be {@code null}). */
  private final InputInfo info;

  /**
   * Constructor.
   * @param ck key facade
   */
  DerFormat(final CryptoKey ck) {
    this.ck = ck;
    info = ck.info;
  }

  /**
   * Parses the first PEM block of the input.
   * @param bytes input
   * @param strict raise an error if the input contains no complete PEM block
   * @return PEM block (can be {@code null})
   * @throws QueryException query exception
   */
  Pem pem(final byte[] bytes, final boolean strict) throws QueryException {
    // Java 25 (once the baseline exceeds 21): check java.security.PEMDecoder (preview in 25)
    if(!contains(bytes, PEM_START)) return null;
    final Matcher m = PEM.matcher(string(bytes));
    // skip EC parameters, as written by "openssl ecparam -genkey"
    boolean found = m.find();
    while(found && m.group(1).equals("EC PARAMETERS")) found = m.find();
    if(!found) {
      if(strict) throw CX_KEYINV_X.get(info, "incomplete or invalid PEM block");
      return null;
    }

    final String label = m.group(1);
    final byte[] der = Base64.decode(token(m.group(2)));
    return switch(label) {
      case "PUBLIC KEY" -> new Pem(der, false, false);
      case "PRIVATE KEY" -> new Pem(der, true, false);
      case "RSA PUBLIC KEY", "RSA PRIVATE KEY" -> {
        // PKCS #1: wrap in SPKI or PKCS #8 structure
        final boolean priv = label.endsWith("PRIVATE KEY");
        yield new Pem(Der.encode(Der.SEQUENCE, priv ?
          concat(Der.VERSION_0, RSA_ALG, Der.encode(Der.OCTET_STRING, der)) :
          concat(RSA_ALG, Der.bitString(der))),
          priv, false);
      }
      case "EC PRIVATE KEY" -> new Pem(sec1(der), true, false);
      case "CERTIFICATE" -> new Pem(der, false, true);
      default -> {
        if(strict) throw CX_KEYINV_X.get(info, "unsupported PEM type '" + label + "'");
        yield null;
      }
    };
  }

  /**
   * Reads a key of the specified type from a PEM block.
   * @param pem PEM block
   * @param type key type
   * @return key
   * @throws QueryException query exception
   * @throws GeneralSecurityException security exception
   */
  Key read(final Pem pem, final Type type) throws QueryException, GeneralSecurityException {
    return pem.cert() ? ck.check(certificate(pem.der()), type) :
      read(pem.der(), type, pem.priv());
  }

  /**
   * Reads a key of any type from a PEM block.
   * @param pem PEM block
   * @return key material
   * @throws GeneralSecurityException security exception
   */
  Keys read(final Pem pem) throws GeneralSecurityException {
    return pem.cert() ? new Keys(null, certificate(pem.der()), null) :
      keys(pem.der(), pem.priv());
  }

  /**
   * Reads a key of the specified type (SPKI, PKCS #8 or X.509 certificate).
   * @param der DER bytes
   * @param type key type
   * @param priv private key
   * @return key
   * @throws QueryException query exception
   * @throws GeneralSecurityException security exception
   */
  Key read(final byte[] der, final Type type, final boolean priv)
      throws QueryException, GeneralSecurityException {
    try {
      return ck.check(generate(type.jca, der, priv), type);
    } catch(final InvalidKeySpecException ex) {
      if(!priv) {
        try {
          return ck.check(certificate(der), type);
        } catch(final CertificateException e) {
          Util.debug(e);
        }
      }
      // other key types: report mismatches
      try {
        return ck.check(generate(der, priv), type);
      } catch(final InvalidKeySpecException e) {
        Util.debug(e);
      }
      throw ex;
    }
  }

  /**
   * Reads a key of any type (SPKI, PKCS #8 or X.509 certificate).
   * @param der DER bytes
   * @return key material
   * @throws QueryException query exception
   * @throws GeneralSecurityException security exception
   */
  Keys read(final byte[] der) throws QueryException, GeneralSecurityException {
    for(final boolean priv : new boolean[] { false, true }) {
      try {
        return keys(der, priv);
      } catch(final InvalidKeySpecException ex) {
        Util.debug(ex);
      }
    }
    try {
      return new Keys(null, certificate(der), null);
    } catch(final CertificateException ex) {
      Util.debug(ex);
    }
    throw CX_KEYINV_X.get(info, "DER key expected");
  }

  /**
   * Returns key material for a DER key, including the public key of private keys if possible.
   * @param der DER bytes
   * @param priv private key
   * @return key material
   * @throws GeneralSecurityException security exception
   */
  private static Keys keys(final byte[] der, final boolean priv)
      throws GeneralSecurityException {
    final Key key = generate(der, priv);
    if(!priv) return new Keys(null, (PublicKey) key, null);

    PublicKey pub = null;
    if(key instanceof final RSAPrivateCrtKey rsa) {
      pub = KeyFactory.getInstance("RSA").generatePublic(
          new RSAPublicKeySpec(rsa.getModulus(), rsa.getPublicExponent()));
    } else if(key instanceof final ECPrivateKey ec) {
      pub = point(ec);
    }
    return new Keys(null, pub, (PrivateKey) key);
  }

  /**
   * Extracts the public key from an X.509 certificate.
   * @param der DER bytes
   * @return public key
   * @throws CertificateException certificate exception
   */
  private static PublicKey certificate(final byte[] der) throws CertificateException {
    return CertificateFactory.getInstance("X.509").generateCertificate(
        new ByteArrayInputStream(der)).getPublicKey();
  }

  /**
   * Writes a key in PEM format.
   * @param keys key material
   * @return PEM string
   * @throws QueryException query exception
   */
  String pem(final Keys keys) throws QueryException {
    // Java 25 (once the baseline exceeds 21): check java.security.PEMEncoder (preview in 25)
    if(keys.secret() != null) throw CX_EXPORT_X.get(info, "secret key as PEM");
    final boolean priv = keys.priv() != null;
    final String label = priv ? "PRIVATE KEY" : "PUBLIC KEY";
    final byte[] der = priv ? pkcs8(keys) : spki(keys);
    return "-----BEGIN " + label + "-----\n" + string(MIME.encode(der)) + "\n-----END " + label +
        "-----\n";
  }

  /**
   * Returns the SPKI encoding of a public key.
   * @param keys key material
   * @return encoded key
   * @throws QueryException query exception
   */
  byte[] spki(final Keys keys) throws QueryException {
    return ck.pub(keys).getEncoded();
  }

  /**
   * Returns the PKCS #8 encoding of a private key (with the public point for EC keys).
   * @param keys key material
   * @return encoded key
   * @throws QueryException query exception
   */
  byte[] pkcs8(final Keys keys) throws QueryException {
    if(keys.priv() == null) throw CX_EXPORT_X.get(info, "no private key available");
    final byte[] der = keys.priv().getEncoded();
    if(!(keys.pub() instanceof final ECPublicKey ec)) return der;

    // add the public point to the SEC1 structure: [1] BIT STRING (as written by OpenSSL)
    final List<Der> pkcs8 = fields(der);
    final Der sec1 = Der.parse(pkcs8.get(2).content());
    if(sec1.child(Der.CONTEXT_1) != null) return der;
    final byte[] content = concat(sec1.content(),
        Der.encode(Der.CONTEXT_1, Der.bitString(CryptoKey.point(ec))));
    return Der.encode(Der.SEQUENCE, concat(pkcs8.get(0).encoded(), pkcs8.get(1).encoded(),
        Der.encode(Der.OCTET_STRING, Der.encode(Der.SEQUENCE, content))));
  }

  /**
   * Wraps a SEC1 EC private key into a PKCS #8 structure.
   * @param der DER bytes
   * @return DER bytes
   * @throws QueryException query exception
   */
  private byte[] sec1(final byte[] der) throws QueryException {
    // ECPrivateKey: version, privateKey, [0] parameters (named curve), [1] publicKey
    final Der params = Der.parse(der).child(Der.CONTEXT_0);
    if(params == null) throw CX_KEYINV_X.get(info, "EC private key without named curve");
    return Der.encode(Der.SEQUENCE, concat(Der.VERSION_0,
      Der.encode(Der.SEQUENCE, concat(EC_OID, params.content())),
      Der.encode(Der.OCTET_STRING, der)));
  }

  /**
   * Returns the public key stored in the optional field [1] of the SEC1 structure of an EC key.
   * @param ec private key
   * @return public key, absent if it is missing, invalid or does not match (can be {@code null})
   */
  private static PublicKey point(final ECPrivateKey ec) {
    try {
      // 00 04 x y
      final Der field = Der.parse(fields(ec.getEncoded()).get(2).content()).child(Der.CONTEXT_1);
      final byte[] point = field != null ? Der.parse(field.content()).content() : null;
      final int l = point != null ? (point.length - 2) / 2 : 0;
      if(l < 1 || point.length != 2 + 2 * l || point[0] != 0 || point[1] != 4) return null;
      final PublicKey pk = KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(
          new ECPoint(new BigInteger(1, point, 2, l), new BigInteger(1, point, 2 + l, l)),
          ec.getParams()));
      // only accept the point if it belongs to the private key
      final byte[] test = token("test");
      final Signature sig = Signature.getInstance("SHA256withECDSA");
      sig.initSign(ec);
      sig.update(test);
      final byte[] signature = sig.sign();
      sig.initVerify(pk);
      sig.update(test);
      return sig.verify(signature) ? pk : null;
    } catch(final GeneralSecurityException | IllegalArgumentException ex) {
      Util.debug(ex);
      return null;
    }
  }

  /**
   * Returns the fields of a PKCS #8 structure (version, algorithm, private key).
   * @param der DER bytes
   * @return fields
   * @throws IllegalArgumentException invalid encoding
   */
  private static List<Der> fields(final byte[] der) {
    final List<Der> fields = Der.parse(der).children();
    if(fields.size() < 3) throw new IllegalArgumentException("Invalid PKCS #8 structure");
    return fields;
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
   * Generates a public or private key of any type from DER bytes.
   * @param der DER bytes
   * @param priv private key
   * @return key
   * @throws GeneralSecurityException security exception
   */
  private static Key generate(final byte[] der, final boolean priv)
      throws GeneralSecurityException {
    InvalidKeySpecException error = null;
    final Iterable<String> jcas = Arrays.stream(Type.values()).filter(tp -> !tp.secret()).
        map(tp -> tp.jca).distinct()::iterator;
    for(final String jca : jcas) {
      try {
        return generate(jca, der, priv);
      } catch(final InvalidKeySpecException ex) {
        if(error == null) error = ex;
      }
    }
    throw error;
  }
}
