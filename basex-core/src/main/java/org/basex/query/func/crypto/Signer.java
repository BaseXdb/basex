package org.basex.query.func.crypto;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.crypto.AlgorithmOptions.*;

import java.security.*;

import javax.crypto.*;

import org.basex.query.*;
import org.basex.query.func.crypto.CryptoAlgorithm.*;
import org.basex.query.func.crypto.CryptoKey.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Creates and verifies signatures and message authentication codes.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param alg algorithm
 * @param hash hash algorithm (can be {@code null})
 * @param salt salt length
 * @param der DER signature format
 * @param info input info (can be {@code null})
 */
record Signer(CryptoAlgorithm alg, Hash hash, int salt, boolean der, InputInfo info) {
  /**
   * Resolves the algorithm options.
   * @param options algorithm options
   * @param info input info (can be {@code null})
   * @return signer
   * @throws QueryException query exception
   */
  static Signer get(final AlgorithmOptions options, final InputInfo info) throws QueryException {
    final CryptoAlgorithm alg = CryptoAlgorithm.get(options, Purpose.SIGN, info);
    final Hash hash = alg.hash(options, info);
    final Integer sl = options.get(SALT_LENGTH);
    if(sl != null && sl < 0) throw CX_INVALGO_X.get(info, alg.name + " with salt-length " + sl);
    final int salt = sl != null ? sl : hash != null ? hash.length : 0;
    return new Signer(alg, hash, salt, options.get(SIGNATURE_FORMAT) == SignatureFormat.DER,
        info);
  }

  /**
   * Signs data.
   * @param data data
   * @param key key item
   * @return signature
   * @throws QueryException query exception
   */
  byte[] sign(final byte[] data, final Item key) throws QueryException {
    final Key k = key(key, Usage.SIGN);
    try {
      if(alg.key.secret()) return mac(data, k);
      final Signature sig = alg.signature(hash, salt, der);
      sig.initSign((PrivateKey) k);
      sig.update(data);
      return sig.sign();
    } catch(final InvalidKeyException ex) {
      throw CX_KEYINV_X.get(info, ex);
    } catch(final SignatureException ex) {
      throw CX_SIGEXC_X.get(info, ex);
    } catch(final GeneralSecurityException ex) {
      throw CX_INVALGO_X.get(info, ex);
    }
  }

  /**
   * Verifies a signature.
   * @param data data
   * @param signature signature
   * @param key key item
   * @return result of check
   * @throws QueryException query exception
   */
  boolean verify(final byte[] data, final byte[] signature, final Item key)
      throws QueryException {
    final Key k = key(key, Usage.VERIFY);
    try {
      if(alg.key.secret()) return MessageDigest.isEqual(mac(data, k), signature);
      final Signature sig = alg.signature(hash, salt, der);
      sig.initVerify((PublicKey) k);
      sig.update(data);
      return sig.verify(signature);
    } catch(final SignatureException ex) {
      // malformed signature
      Util.debug(ex);
      return false;
    } catch(final InvalidKeyException ex) {
      throw CX_KEYINV_X.get(info, ex);
    } catch(final GeneralSecurityException ex) {
      throw CX_INVALGO_X.get(info, ex);
    }
  }

  /**
   * Imports the key.
   * @param key key item
   * @param usage key usage
   * @return key
   * @throws QueryException query exception
   */
  private Key key(final Item key, final Usage usage) throws QueryException {
    return new CryptoKey(info).get(key, alg.key, usage, alg.jwk(hash));
  }

  /**
   * Computes a message authentication code.
   * @param data data
   * @param key secret key
   * @return MAC
   * @throws GeneralSecurityException security exception
   */
  private byte[] mac(final byte[] data, final Key key) throws GeneralSecurityException {
    final Mac mac = Mac.getInstance(hash.hmac);
    mac.init(key);
    return mac.doFinal(data);
  }
}
