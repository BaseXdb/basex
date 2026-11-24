package org.basex.query.func.crypto;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.crypto.AlgorithmOptions.*;

import java.security.*;
import java.security.spec.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.func.crypto.CryptoKey.Type;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Registry of cryptographic algorithms (W3C Web Cryptography API names).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
enum CryptoAlgorithm {
  /** HMAC. */
  HMAC("HMAC", Purpose.SIGN, Type.HMAC, LENGTH, HASH),
  /** RSA PKCS#1 v1.5 signatures. */
  RSASSA_PKCS1_V1_5("RSASSA-PKCS1-v1_5", Purpose.SIGN, Type.RSA, MODULUS_LENGTH, HASH),
  /** RSA PSS signatures. */
  RSA_PSS("RSA-PSS", Purpose.SIGN, Type.RSA, MODULUS_LENGTH, HASH, SALT_LENGTH),
  /** ECDSA signatures. */
  ECDSA("ECDSA", Purpose.SIGN, Type.EC, NAMED_CURVE, HASH, SIGNATURE_FORMAT),
  /** Ed25519 signatures. */
  ED25519("Ed25519", Purpose.SIGN, Type.ED25519, null),
  /** Ed448 signatures. */
  ED448("Ed448", Purpose.SIGN, Type.ED448, null),
  /** AES in CBC mode. */
  AES_CBC("AES-CBC", Purpose.ENCRYPT, Type.AES, LENGTH, IV),
  /** AES in GCM mode. */
  AES_GCM("AES-GCM", Purpose.ENCRYPT, Type.AES, LENGTH, IV, ADDITIONAL_DATA, TAG_LENGTH),
  /** RSA with OAEP padding. */
  RSA_OAEP("RSA-OAEP", Purpose.ENCRYPT, Type.RSA, MODULUS_LENGTH, HASH),
  /** HKDF key derivation. */
  HKDF("HKDF", Purpose.DERIVE, Type.HMAC, null, HASH, SALT, INFO),
  /** ECDH key agreement. */
  ECDH("ECDH", Purpose.DERIVE, Type.EC, NAMED_CURVE, PUBLIC),
  /** X25519 key agreement. */
  X25519("X25519", Purpose.DERIVE, Type.X25519, null, PUBLIC);

  /** Purposes of algorithms. */
  enum Purpose {
    /** Signatures and message authentication codes. */ SIGN,
    /** Encryption. */ ENCRYPT,
    /** Key derivation. */ DERIVE
  }

  /** Hash algorithms. */
  enum Hash {
    /** SHA-1. */
    SHA_1(20, 64),
    /** SHA-256. */
    SHA_256(32, 64),
    /** SHA-384. */
    SHA_384(48, 128),
    /** SHA-512. */
    SHA_512(64, 128);

    /** Hash length in bytes. */
    final int length;
    /** Block size in bytes. */
    final int block;
    /** JCA name of the HMAC algorithm ({@code HmacSHA256}). */
    final String hmac;
    /** JCA name ({@code SHA-256}). */
    final String jca;
    /** Compact name ({@code SHA256}). */
    final String compact;

    /**
     * Constructor.
     * @param length hash length in bytes
     * @param block block size in bytes
     */
    Hash(final int length, final int block) {
      this.length = length;
      this.block = block;
      jca = name().replace('_', '-');
      compact = name().replace("_", "");
      hmac = "Hmac" + compact;
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

    /**
     * Returns the hash algorithm with the specified name or raises an error.
     * @param name name
     * @param info input info (can be {@code null})
     * @return hash algorithm
     * @throws QueryException query exception
     */
    static Hash get(final String name, final InputInfo info) throws QueryException {
      final Hash hash = get(name);
      if(hash == null) throw CX_INVHASH_X.get(info, name);
      return hash;
    }
  }

  /** Algorithm name. */
  final String name;
  /** Purpose. */
  final Purpose purpose;
  /** Key type. */
  final Type key;
  /** Parameter for key generation (can be {@code null}). */
  final Option<?> generation;
  /** Supported parameters (besides the name). */
  final List<Option<?>> params;

  /**
   * Constructor.
   * @param name algorithm name
   * @param purpose purpose
   * @param key key type
   * @param generation parameter for key generation (can be {@code null})
   * @param params supported parameters
   */
  CryptoAlgorithm(final String name, final Purpose purpose, final Type key,
      final Option<?> generation, final Option<?>... params) {
    this.name = name;
    this.purpose = purpose;
    this.key = key;
    this.generation = generation;
    this.params = List.of(params);
  }

  /**
   * Returns the algorithm specified by the options and checks its parameters.
   * @param options algorithm options
   * @param purpose required purpose, key generation if absent (can be {@code null})
   * @param info input info (can be {@code null})
   * @return algorithm
   * @throws QueryException query exception
   */
  static CryptoAlgorithm get(final AlgorithmOptions options, final Purpose purpose,
      final InputInfo info) throws QueryException {
    final String name = options.get(NAME);
    if(name == null) throw CX_INVALGO_X.get(info, "no name supplied");
    return get(name, options, purpose, info);
  }

  /**
   * Returns the algorithm with the specified name and checks its parameters.
   * @param name algorithm name
   * @param options algorithm options
   * @param purpose required purpose, key generation if absent (can be {@code null})
   * @param info input info (can be {@code null})
   * @return algorithm
   * @throws QueryException query exception
   */
  static CryptoAlgorithm get(final String name, final AlgorithmOptions options,
      final Purpose purpose, final InputInfo info) throws QueryException {
    for(final CryptoAlgorithm alg : values()) {
      if(!alg.name.equalsIgnoreCase(name)) continue;
      if(purpose != null && alg.purpose != purpose) {
        throw CX_INVALGO_X.get(info, alg.name + " for " + Enums.string(purpose));
      }
      for(final Option<?> option : options) {
        // key generation: generation parameter and hash; otherwise: operation parameters
        final boolean supported = purpose != null ? alg.params.contains(option) :
          option == alg.generation || option == HASH && alg.params.contains(HASH);
        if(option != NAME && options.modified(option) && !supported) {
          throw CX_INVALGO_X.get(info, alg.name + " with parameter '" + option.name() + "'");
        }
      }
      return alg;
    }
    throw CX_INVALGO_X.get(info, name);
  }

  /**
   * Returns the hash algorithm specified by the options.
   * @param options algorithm options
   * @param info input info (can be {@code null})
   * @return hash algorithm, absent if the algorithm takes none (can be {@code null})
   * @throws QueryException query exception
   */
  Hash hash(final AlgorithmOptions options, final InputInfo info) throws QueryException {
    final String hash = options.get(HASH);
    if(hash != null) return Hash.get(hash, info);
    if(params.contains(HASH)) throw CX_INVALGO_X.get(info, name + " without hash");
    return null;
  }

  /**
   * Returns the compatible JWK algorithm names (RFC 7518, RFC 8037, Web Cryptography API).
   * @param hash hash algorithm (can be {@code null})
   * @return names, absent if none are defined (can be {@code null})
   */
  List<String> jwk(final Hash hash) {
    final int bits = hash != null ? hash.length * 8 : 0;
    return switch(this) {
      case HMAC -> List.of("HS" + bits);
      case RSASSA_PKCS1_V1_5 -> List.of("RS" + bits);
      case RSA_PSS -> List.of("PS" + bits);
      case ECDSA -> List.of("ES" + bits);
      case ED25519, ED448 -> List.of("EdDSA", name);
      case AES_CBC -> List.of("A128CBC", "A192CBC", "A256CBC");
      case AES_GCM -> List.of("A128GCM", "A192GCM", "A256GCM");
      case RSA_OAEP -> List.of(hash == Hash.SHA_1 ? "RSA-OAEP" : "RSA-OAEP-" + bits);
      case ECDH, X25519 -> List.of("ECDH-ES", "ECDH-ES+A128KW", "ECDH-ES+A192KW",
          "ECDH-ES+A256KW");
      default -> null;
    };
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
