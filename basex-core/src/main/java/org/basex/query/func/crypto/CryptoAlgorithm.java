package org.basex.query.func.crypto;

import static org.basex.query.func.crypto.AlgorithmOptions.*;

import java.security.*;
import java.security.spec.*;
import java.util.*;

import org.basex.query.func.crypto.CryptoKey.Type;
import org.basex.util.options.*;

/**
 * Registry of cryptographic algorithms (W3C Web Cryptography API names).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
enum CryptoAlgorithm {
  /** HMAC. */
  HMAC("HMAC", Type.HMAC, "HS", HASH),
  /** RSA PKCS#1 v1.5 signatures. */
  RSASSA_PKCS1_V1_5("RSASSA-PKCS1-v1_5", Type.RSA, "RS", HASH),
  /** RSA PSS signatures. */
  RSA_PSS("RSA-PSS", Type.RSA, "PS", HASH, SALT_LENGTH),
  /** ECDSA signatures. */
  ECDSA("ECDSA", Type.EC, "ES", HASH, SIGNATURE_FORMAT),
  /** Ed25519 signatures. */
  ED25519("Ed25519", Type.ED25519, null),
  /** Ed448 signatures. */
  ED448("Ed448", Type.ED448, null);

  /** Hash algorithms. */
  enum Hash {
    /** SHA-1. */
    SHA_1(20),
    /** SHA-256. */
    SHA_256(32),
    /** SHA-384. */
    SHA_384(48),
    /** SHA-512. */
    SHA_512(64);

    /** Hash length in bytes. */
    final int length;
    /** JCA name ({@code SHA-256}). */
    final String jca;
    /** Compact name ({@code SHA256}). */
    final String compact;

    /**
     * Constructor.
     * @param length hash length in bytes
     */
    Hash(final int length) {
      this.length = length;
      jca = name().replace('_', '-');
      compact = name().replace("_", "");
    }

    /**
     * Returns the hash algorithm with the specified name (case-insensitive, hyphen optional).
     * @param name name
     * @return hash algorithm (can be {@code null})
     */
    static Hash get(final String name) {
      final String nm = name.replace("-", "");
      for(final Hash hash : values()) {
        if(hash.compact.equalsIgnoreCase(nm)) return hash;
      }
      return null;
    }
  }

  /** Algorithm name. */
  final String name;
  /** Key type. */
  final Type key;
  /** Prefix of the JWK algorithm names (can be {@code null}). */
  final String jwk;
  /** Supported parameters (besides the name). */
  final List<Option<?>> params;

  /**
   * Constructor.
   * @param name algorithm name
   * @param key key type
   * @param jwk prefix of the JWK algorithm names (can be {@code null})
   * @param params supported parameters
   */
  CryptoAlgorithm(final String name, final Type key, final String jwk, final Option<?>... params) {
    this.name = name;
    this.key = key;
    this.jwk = jwk;
    this.params = List.of(params);
  }

  /**
   * Returns the algorithm with the specified name (case-insensitive).
   * @param name name
   * @return algorithm (can be {@code null})
   */
  static CryptoAlgorithm get(final String name) {
    for(final CryptoAlgorithm alg : values()) {
      if(alg.name.equalsIgnoreCase(name)) return alg;
    }
    return null;
  }

  /**
   * Returns a signature instance.
   * @param hash hash algorithm (can be {@code null})
   * @param salt salt length
   * @param der DER signature format
   * @return signature
   * @throws GeneralSecurityException security exception
   */
  Signature signature(final Hash hash, final int salt, final boolean der)
      throws GeneralSecurityException {
    return switch(this) {
      case RSASSA_PKCS1_V1_5 -> Signature.getInstance(hash.compact + "withRSA");
      case RSA_PSS -> {
        final Signature sig = Signature.getInstance("RSASSA-PSS");
        sig.setParameter(new PSSParameterSpec(hash.jca, "MGF1", new MGF1ParameterSpec(hash.jca),
            salt, PSSParameterSpec.TRAILER_FIELD_BC));
        yield sig;
      }
      case ECDSA -> Signature.getInstance(hash.compact + "withECDSA" +
          (der ? "" : "inP1363Format"));
      default -> Signature.getInstance(name);
    };
  }
}
