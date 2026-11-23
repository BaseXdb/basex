package org.basex.query.func.crypto;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.crypto.AlgorithmOptions.*;
import static org.basex.util.Token.*;

import java.security.*;
import java.security.spec.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.basex.query.*;
import org.basex.query.func.crypto.CryptoAlgorithm.*;
import org.basex.query.func.crypto.CryptoKey.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.Base64;

/**
 * This class encrypts and decrypts textual inputs.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
final class Encryption {
  /** String: symmetric. */
  private static final String SYMMETRIC = "symmetric";
  /** String: asymmetric. */
  private static final String ASYMMETRIC = "asymmetric";
  /** String: base64. */
  private static final String BASE64 = "base64";
  /** String: hex. */
  private static final String HEX = "hex";

  /** Input info (can be {@code null}). */
  private final InputInfo info;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   */
  Encryption(final InputInfo info) {
    this.info = info;
  }

  /**
   * Encrypts or decrypts the given input.
   * @param data data to process
   * @param type encryption type
   * @param key key item: secret key, or public (encrypt) or private (decrypt) key
   * @param options algorithm options
   * @param encrypt encrypt or decrypt
   * @param binary return decrypted data as binary item
   * @return encrypted or decrypted input
   * @throws QueryException query exception
   */
  Item encryption(final byte[] data, final String type, final Item key,
      final AlgorithmOptions options, final boolean encrypt, final boolean binary)
      throws QueryException {

    // BaseX 14.0: drop $type, which is determined by the algorithm
    final boolean symmetric = type.equals(SYMMETRIC);
    if(!symmetric && !type.equals(ASYMMETRIC)) throw CX_ENCTYP_X.get(info, type);
    final String name = options.get(NAME);
    if(name == null) throw CX_INVALGO_X.get(info, "no name supplied");

    // legacy names: AES (alias of AES-CBC), RSA (OAEP with SHA-256, but MGF1 with SHA-1)
    final boolean rsa = name.equalsIgnoreCase("RSA"), legacy = rsa || name.equalsIgnoreCase("AES");
    final CryptoAlgorithm alg = CryptoAlgorithm.get(rsa ? "RSA-OAEP" : legacy ? "AES-CBC" : name,
        options, Purpose.ENCRYPT, info);
    if(symmetric != alg.key.secret()) throw CX_INVALGO_X.get(info, name);
    if(rsa && options.get(HASH) != null) {
      throw CX_INVALGO_X.get(info, name + " with parameter 'hash'");
    }
    final Hash hash = rsa ? Hash.SHA_256 : alg.hash(options, info);

    final Usage usage = encrypt ? Usage.ENCRYPT : Usage.DECRYPT;
    final Key k = new CryptoKey(info).get(key, alg.key, usage, legacy ? null : alg.jwk(hash));
    final int mode = encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
    try {
      final byte[] result;
      if(alg == CryptoAlgorithm.RSA_OAEP) {
        final Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        cipher.init(mode, k, new OAEPParameterSpec(hash.jca, "MGF1",
            rsa ? MGF1ParameterSpec.SHA1 : new MGF1ParameterSpec(hash.jca),
            PSource.PSpecified.DEFAULT));
        result = cipher.doFinal(data);
      } else {
        final boolean gcm = alg == CryptoAlgorithm.AES_GCM;
        final Cipher cipher = Cipher.getInstance(gcm ? "AES/GCM/NoPadding" :
          "AES/CBC/PKCS5Padding");
        // without explicit initialization vector: generate it and prepend it to the result
        byte[] iv = options.bytes(IV, info);
        final boolean generated = iv == null;
        final int ivl = gcm ? 12 : 16;
        if(generated) iv = encrypt ? CryptoRandomBytes.get(ivl) : substring(data, 0, ivl);
        final int off = generated && !encrypt ? iv.length : 0;
        final Integer tl = options.get(TAG_LENGTH);
        final int tag = tl != null ? tl : 128;
        if(tag < 96 || tag > 128 || tag % 8 != 0) {
          throw CX_INVALGO_X.get(info, alg.name + " with tag-length " + tag);
        }
        cipher.init(mode, k, gcm ? new GCMParameterSpec(tag, iv) : new IvParameterSpec(iv));
        final byte[] aad = options.bytes(ADDITIONAL_DATA, info);
        if(aad != null) cipher.updateAAD(aad);
        if(encrypt && generated) {
          // write the encrypted data behind the initialization vector
          final byte[] output = Arrays.copyOf(iv, ivl + cipher.getOutputSize(data.length));
          final int n = cipher.doFinal(data, 0, data.length, output, ivl);
          result = n == output.length - ivl ? output : Arrays.copyOf(output, ivl + n);
        } else {
          result = cipher.doFinal(data, off, data.length - off);
        }
      }
      return encrypt || binary ? B64.get(result) : Str.get(result);
    } catch(final NoSuchPaddingException ex) {
      throw CX_NOPAD_X.get(info, ex);
    } catch(final BadPaddingException ex) {
      throw CX_BADPAD_X.get(info, ex);
    } catch(final IllegalArgumentException | InvalidKeyException ex) {
      throw CX_KEYINV_X.get(info, ex);
    } catch(final IllegalBlockSizeException ex) {
      throw CX_ILLBLO_X.get(info, ex);
    } catch(final GeneralSecurityException ex) {
      throw CX_INVALGO_X.get(info, ex);
    }
  }

  /**
   * Creates a message authentication code (MAC) for the given input.
   * @param data data to process
   * @param key secret key
   * @param algorithm encryption algorithm
   * @param encoding encoding (can be {@code null})
   * @return MAC
   * @throws QueryException query exception
   */
  Str hmac(final byte[] data, final byte[] key, final String algorithm, final String encoding)
      throws QueryException {

    final boolean b64 = encoding == null || encoding.equals(BASE64);
    if(!b64 && !encoding.equals(HEX)) throw CX_ENC_X.get(info, encoding);

    try {
      final Key kspec = new SecretKeySpec(key, algorithm);
      final Mac mac = Mac.getInstance("hmac" + algorithm);
      mac.init(kspec);
      final byte[] hash = mac.doFinal(data);
      // convert to specified encoding, base64 as a standard, else use hex
      return Str.get(b64 ? Base64.encode(hash) : hex(hash, true));
    } catch(final NoSuchAlgorithmException ex) {
      throw CX_INVHASH_X.get(info, algorithm).cause(ex);
    } catch(final IllegalArgumentException | InvalidKeyException ex) {
      throw CX_KEYINV_X.get(info, ex);
    }
  }
}
