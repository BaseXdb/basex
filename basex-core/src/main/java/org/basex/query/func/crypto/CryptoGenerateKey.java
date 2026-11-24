package org.basex.query.func.crypto;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.crypto.AlgorithmOptions.*;

import java.security.*;
import java.security.spec.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.crypto.CryptoAlgorithm.*;
import org.basex.query.func.crypto.CryptoKey.*;
import org.basex.query.value.map.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CryptoGenerateKey extends StandardFunc {
  /** Maximum length of RSA moduli in bits. */
  private static final int MAX_RSA = 16384;
  /** Maximum length of HMAC keys in bits. */
  private static final int MAX_HMAC = 8192;

  @Override
  public XQMap value(final QueryContext qc) throws QueryException {
    final AlgorithmOptions options = toOptions(qc);
    final CryptoAlgorithm alg = CryptoAlgorithm.get(options, null, info);
    // validate the hash, even if it is not required for generating the key
    final Hash hash = options.get(HASH) != null || alg == CryptoAlgorithm.HMAC ?
      alg.hash(options, info) : null;
    if(alg == CryptoAlgorithm.HKDF) throw CX_INVALGO_X.get(info, alg.name + " key generation");

    final CryptoKey ck = new CryptoKey(info);
    if(alg.key.secret()) {
      final Integer length = options.get(LENGTH);
      final int bits;
      if(alg == CryptoAlgorithm.HMAC) {
        // default length: block size of the hash function; minimum: hash length (RFC 2104)
        bits = length != null ? length : hash.block * 8;
        if(bits < hash.length * 8 || bits > MAX_HMAC || bits % 8 != 0) {
          throw CX_INVALGO_X.get(info, alg.name + " with length " + bits + " (" +
              hash.length * 8 + "-" + MAX_HMAC + ", multiple of 8)");
        }
      } else {
        bits = length != null ? length : 256;
        if(bits != 128 && bits != 192 && bits != 256) {
          throw CX_INVALGO_X.get(info, alg.name + " with length " + bits);
        }
      }
      final byte[] secret = CryptoRandomBytes.get(bits / 8);
      return ck.jwk(new Keys(ck.secret(secret, alg.key), null, null));
    }

    try {
      final KeyPairGenerator kpg = KeyPairGenerator.getInstance(alg.key.curve != null ?
        alg.key.curve : alg.key.jca);
      if(alg.key == Type.RSA) {
        // key generation cannot be interrupted: limit the modulus length
        final Integer length = options.get(MODULUS_LENGTH);
        final int bits = length != null ? length : 2048;
        if(bits > MAX_RSA) {
          throw CX_INVALGO_X.get(info, alg.name + " with modulus-length " + bits + " (maximum: " +
              MAX_RSA + ')');
        }
        kpg.initialize(new RSAKeyGenParameterSpec(bits, RSAKeyGenParameterSpec.F4));
      } else if(alg.key == Type.EC) {
        final String crv = options.get(NAMED_CURVE), name = crv != null ?
          JwkFormat.CURVES.get(crv) : null;
        if(name == null) throw CX_INVALGO_X.get(info, alg.name + (crv == null ?
          " without named-curve" : " with named-curve '" + crv + "'"));
        kpg.initialize(new ECGenParameterSpec(name));
      }
      final KeyPair pair = kpg.generateKeyPair();
      return new MapBuilder().
        put("public", ck.jwk(new Keys(null, pair.getPublic(), null))).
        put("private", ck.jwk(new Keys(null, pair.getPublic(), pair.getPrivate()))).map();
    } catch(final GeneralSecurityException | InvalidParameterException ex) {
      throw CX_INVALGO_X.get(info, ex);
    }
  }
}
