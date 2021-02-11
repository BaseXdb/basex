package org.basex.query.func.crypto;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.security.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.Base64;

/**
 * This class encrypts and decrypts textual inputs.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
final class Encryption {
  /** Exact encryption algorithm JAVA names. */
  private static final HashMap<String, String> TRANSFORMATIONS = new HashMap<>();
  /** Supported encryption algorithms, mapped to correct IV lengths. */
  private static final HashMap<String, Integer> IVLENGTHS = new HashMap<>();

  /** String: symmetric. */
  private static final String SYMMETRIC = "symmetric";
  /** String: base64. */
  private static final String BASE64 = "base64";
  /** String: hex. */
  private static final String HEX = "hex";

  /** Input info. */
  private final InputInfo info;

  static {
    TRANSFORMATIONS.put("DES", "DES/CBC/PKCS5Padding");
    TRANSFORMATIONS.put("AES", "AES/CBC/PKCS5Padding");
    IVLENGTHS.put("DES", 8);
    IVLENGTHS.put("AES", 16);
  }

  /**
   * Constructor.
   * @param info input info
   */
  Encryption(final InputInfo info) {
    this.info = info;
  }

  /**
   * Encrypts or decrypts the given input.
   * @param data data to process
   * @param type encryption type
   * @param key secret key
   * @param algorithm encryption algorithm
   * @param encrypt encrypt or decrypt
   * @return encrypted or decrypted input
   * @throws QueryException query exception
   */
  Item encryption(final byte[] data, final String type, final byte[] key, final String algorithm,
      final boolean encrypt) throws QueryException {

    if(!type.equals(SYMMETRIC)) throw CX_ENCTYP_X.get(info, type);

    final String transformation = TRANSFORMATIONS.get(algorithm);
    if(transformation == null) throw CX_INVALGO_X.get(info, algorithm);
    final int ivl = IVLENGTHS.get(algorithm);

    try {
      final Key kspec = new SecretKeySpec(key, algorithm);
      final Cipher cipher = Cipher.getInstance(transformation);
      return encrypt ? encrypt(data, kspec, cipher, ivl) : decrypt(data, kspec, cipher, ivl);
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
   * Encrypts the given input data.
   * @param data data to encrypt
   * @param kspec key specification
   * @param cipher cipher
   * @param ivl initialization vector length
   * @return encrypted data
   * @throws GeneralSecurityException general security exception
   */
  private static B64 encrypt(final byte[] data, final Key kspec, final Cipher cipher,
      final int ivl) throws GeneralSecurityException {

    // generate random iv. random iv is necessary to make the encryption of a
    // string look different every time it is encrypted.
    final byte[] iv = new byte[ivl];
    SecureRandom.getInstance("SHA1PRNG").nextBytes(iv);

    // encrypt/decrypt
    final IvParameterSpec ivspec = new IvParameterSpec(iv);
    cipher.init(Cipher.ENCRYPT_MODE, kspec, ivspec);
    final byte[] ciph = cipher.doFinal(data);

    // initialization vector is appended to the message for later decryption
    return B64.get(concat(iv, ciph));
  }

  /**
   * Decrypts the given input data.
   * @param data data to decrypt
   * @param kspec key specification
   * @param cipher cipher
   * @param ivl initialization vector length
   * @return decrypted data
   * @throws GeneralSecurityException general security exception
   */
  private static Str decrypt(final byte[] data, final Key kspec, final Cipher cipher,
      final int ivl) throws GeneralSecurityException {

    final byte[] iv = substring(data, 0, ivl);
    final byte[] input = substring(data, ivl, data.length);

    final IvParameterSpec ivspec = new IvParameterSpec(iv);
    cipher.init(Cipher.DECRYPT_MODE, kspec, ivspec);
    final byte[] ciph = cipher.doFinal(input);

    return Str.get(ciph);
  }

  /**
   * Creates a message authentication code (MAC) for the given input.
   * @param data data to process
   * @param key secret key
   * @param algorithm encryption algorithm
   * @param encoding encoding
   * @return MAC
   * @throws QueryException query exception
   */
  Item hmac(final byte[] data, final byte[] key, final String algorithm, final String encoding)
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
      throw CX_INVHASH_X.get(info, algorithm);
    } catch(final IllegalArgumentException | InvalidKeyException ex) {
      throw CX_KEYINV_X.get(info, ex);
    }
  }
}
