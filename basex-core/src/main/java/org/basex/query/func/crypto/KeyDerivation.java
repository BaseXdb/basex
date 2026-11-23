package org.basex.query.func.crypto;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.crypto.AlgorithmOptions.*;
import static org.basex.util.Token.*;

import java.security.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.basex.query.*;
import org.basex.query.func.crypto.CryptoAlgorithm.*;
import org.basex.query.func.crypto.CryptoKey.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Derives bits from keys (HKDF, ECDH, X25519).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class KeyDerivation {
  /** Input info (can be {@code null}). */
  private final InputInfo info;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   */
  KeyDerivation(final InputInfo info) {
    this.info = info;
  }

  /**
   * Derives bits from a key.
   * @param key key item
   * @param options algorithm options
   * @param length length of the result in bytes (-1: default length)
   * @return derived bytes
   * @throws QueryException query exception
   */
  byte[] derive(final Item key, final AlgorithmOptions options, final int length)
      throws QueryException {
    final CryptoAlgorithm alg = CryptoAlgorithm.get(options, Purpose.DERIVE, info);
    final CryptoKey ck = new CryptoKey(info);
    final Key own = ck.get(key, alg.key, Usage.DERIVE, alg.jwk(null));
    try {
      if(alg == CryptoAlgorithm.HKDF) {
        final Hash hash = alg.hash(options, info);
        final int l = length == -1 ? hash.length : length;
        if(l > 255 * hash.length) throw CX_INVALGO_X.get(info, alg.name + " with length " + l);
        return hkdf(hash, own.getEncoded(), options.bytes(SALT, info), options.bytes(INFO, info),
            l);
      }

      // key agreement
      final Value pub = options.get(PUBLIC);
      if(pub.isEmpty()) throw CX_INVALGO_X.get(info, alg.name + " without public key");
      final Key peer = ck.get((Item) pub, alg.key, Usage.PEER, alg.jwk(null));
      final KeyAgreement ka = KeyAgreement.getInstance(alg == CryptoAlgorithm.ECDH ? "ECDH" :
        "XDH");
      ka.init(own);
      ka.doPhase(peer, true);
      final byte[] secret = ka.generateSecret();
      if(length > secret.length) {
        throw CX_INVALGO_X.get(info, alg.name + " with length " + length + " (maximum: " +
            secret.length + ')');
      }
      return length == -1 ? secret : Arrays.copyOf(secret, length);
    } catch(final InvalidKeyException | IllegalStateException ex) {
      throw CX_KEYINV_X.get(info, ex);
    } catch(final GeneralSecurityException ex) {
      throw CX_INVALGO_X.get(info, ex);
    }
  }

  /**
   * Derives a key with HKDF (RFC 5869).
   * @param hash hash algorithm
   * @param ikm input keying material
   * @param salt salt (can be {@code null})
   * @param context context information (can be {@code null})
   * @param length length of the output in bytes
   * @return output keying material
   * @throws GeneralSecurityException security exception
   */
  private static byte[] hkdf(final Hash hash, final byte[] ikm, final byte[] salt,
      final byte[] context, final int length) throws GeneralSecurityException {
    // Java 25 (once the baseline exceeds 21): replace with javax.crypto.KDF ("HKDF-SHA256" etc.)
    final String name = hash.hmac;
    final Mac mac = Mac.getInstance(name);
    // extract
    mac.init(new SecretKeySpec(salt == null || salt.length == 0 ? new byte[hash.length] : salt,
        name));
    final byte[] prk = mac.doFinal(ikm);
    // expand
    mac.init(new SecretKeySpec(prk, name));
    final byte[] okm = new byte[length];
    byte[] t = EMPTY;
    for(int p = 0, i = 1; p < length; i++) {
      mac.update(t);
      if(context != null) mac.update(context);
      mac.update((byte) i);
      t = mac.doFinal();
      final int n = Math.min(t.length, length - p);
      System.arraycopy(t, 0, okm, p, n);
      p += n;
    }
    return okm;
  }
}
