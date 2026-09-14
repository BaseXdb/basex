package org.basex.query.func.crypto;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.security.*;
import java.security.spec.*;
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
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
final class Encryption {
  /** Cipher algorithms. */
  private enum Algorithm {
    /** AES in CBC mode. */
    AES("AES/CBC/PKCS5Padding", 16),
    /** AES in GCM mode. */
    AES_GCM("AES/GCM/NoPadding", 12),
    /** RSA with OAEP padding. */
    RSA("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", 0);

    /** Transformation. */
    private final String transformation;
    /** Length of the initialization vector (0 for asymmetric algorithms). */
    private final int ivl;

    /**
     * Constructor.
     * @param transformation transformation
     * @param ivl length of the initialization vector
     */
    Algorithm(final String transformation, final int ivl) {
      this.transformation = transformation;
      this.ivl = ivl;
    }

    /**
     * Returns the algorithm with the specified name.
     * @param name algorithm name
     * @param symmetric symmetric algorithm
     * @return algorithm, or {@code null} if the name is unknown
     */
    static Algorithm get(final String name, final boolean symmetric) {
      for(final Algorithm alg : values()) {
        if(alg.toString().equals(name) && symmetric == (alg.ivl > 0)) return alg;
      }
      return null;
    }

    @Override
    public String toString() {
      return name().replace('_', '-');
    }
  }

  /** String: symmetric. */
  private static final String SYMMETRIC = "symmetric";
  /** String: asymmetric. */
  private static final String ASYMMETRIC = "asymmetric";
  /** String: base64. */
  private static final String BASE64 = "base64";
  /** String: hex. */
  private static final String HEX = "hex";
  /** Length of the GCM authentication tag in bits. */
  private static final int GCM_TAG_LENGTH = 128;

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
   * @param key secret key, or the public (encrypt) or private (decrypt) key
   * @param algorithm encryption algorithm
   * @param encrypt encrypt or decrypt
   * @return encrypted or decrypted input
   * @throws QueryException query exception
   */
  Item encryption(final byte[] data, final String type, final byte[] key, final String algorithm,
      final boolean encrypt) throws QueryException {

    final boolean symmetric = type.equals(SYMMETRIC);
    if(!symmetric && !type.equals(ASYMMETRIC)) throw CX_ENCTYP_X.get(info, type);
    final Algorithm alg = Algorithm.get(algorithm.toUpperCase(Locale.ENGLISH), symmetric);
    if(alg == null) throw CX_INVALGO_X.get(info, algorithm);

    try {
      final Key kspec = symmetric ? new SecretKeySpec(key, "AES") : rsaKey(key, encrypt);
      final Cipher cipher = Cipher.getInstance(alg.transformation);
      return encrypt ? encrypt(data, kspec, cipher, alg) : decrypt(data, kspec, cipher, alg);
    } catch(final NoSuchPaddingException ex) {
      throw CX_NOPAD_X.get(info, ex);
    } catch(final BadPaddingException ex) {
      throw CX_BADPAD_X.get(info, ex);
    } catch(final IllegalArgumentException | InvalidKeyException | InvalidKeySpecException ex) {
      throw CX_KEYINV_X.get(info, ex);
    } catch(final IllegalBlockSizeException ex) {
      throw CX_ILLBLO_X.get(info, ex);
    } catch(final GeneralSecurityException ex) {
      throw CX_INVALGO_X.get(info, ex);
    }
  }

  /**
   * Parses an RSA key in PEM or DER format.
   * @param key key
   * @param pub public or private key
   * @return key
   * @throws GeneralSecurityException general security exception
   */
  private static Key rsaKey(final byte[] key, final boolean pub) throws GeneralSecurityException {
    // PEM: strip header and footer, decode base64 body
    final byte[] der = startsWith(key, token("-----")) ?
      Base64.decode(token(string(key).replaceAll("-----[^\n]*", ""))) : key;
    final KeyFactory kf = KeyFactory.getInstance("RSA");
    return pub ? kf.generatePublic(new X509EncodedKeySpec(der)) :
      kf.generatePrivate(new PKCS8EncodedKeySpec(der));
  }

  /**
   * Encrypts the given input data.
   * @param data data to encrypt
   * @param kspec key specification
   * @param cipher cipher
   * @param alg algorithm
   * @return encrypted data
   * @throws GeneralSecurityException general security exception
   */
  private static B64 encrypt(final byte[] data, final Key kspec, final Cipher cipher,
      final Algorithm alg) throws GeneralSecurityException {

    if(alg.ivl == 0) {
      cipher.init(Cipher.ENCRYPT_MODE, kspec);
      return B64.get(cipher.doFinal(data));
    }

    // random iv: encryptions of the same input look different every time
    final byte[] iv = new byte[alg.ivl];
    new SecureRandom().nextBytes(iv);
    cipher.init(Cipher.ENCRYPT_MODE, kspec, params(alg, iv));
    // initialization vector is prepended to the message for later decryption
    return B64.get(concat(iv, cipher.doFinal(data)));
  }

  /**
   * Decrypts the given input data.
   * @param data data to decrypt
   * @param kspec key specification
   * @param cipher cipher
   * @param alg algorithm
   * @return decrypted data
   * @throws GeneralSecurityException general security exception
   */
  private static Str decrypt(final byte[] data, final Key kspec, final Cipher cipher,
      final Algorithm alg) throws GeneralSecurityException {

    if(alg.ivl == 0) {
      cipher.init(Cipher.DECRYPT_MODE, kspec);
      return Str.get(cipher.doFinal(data));
    }

    final byte[] iv = substring(data, 0, alg.ivl);
    final byte[] input = substring(data, alg.ivl, data.length);
    cipher.init(Cipher.DECRYPT_MODE, kspec, params(alg, iv));
    return Str.get(cipher.doFinal(input));
  }

  /**
   * Returns the parameter specification for a symmetric algorithm.
   * @param alg algorithm
   * @param iv initialization vector
   * @return parameter specification
   */
  private static AlgorithmParameterSpec params(final Algorithm alg, final byte[] iv) {
    return alg == Algorithm.AES_GCM ? new GCMParameterSpec(GCM_TAG_LENGTH, iv) :
      new IvParameterSpec(iv);
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

  /**
   * Derives a key from a password with PBKDF2.
   * @param password password
   * @param salt salt
   * @param iterations number of iterations
   * @param length length of the derived key in bytes
   * @param algorithm hash algorithm (can be {@code null})
   * @return derived key
   * @throws QueryException query exception
   */
  Hex pbkdf2(final String password, final byte[] salt, final int iterations, final int length,
      final String algorithm) throws QueryException {

    final String alg = algorithm == null ? "SHA256" :
      algorithm.toUpperCase(Locale.ENGLISH).replace("-", "");
    try {
      final SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmac" + alg);
      final PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, length * 8);
      return new Hex(skf.generateSecret(spec).getEncoded());
    } catch(final NoSuchAlgorithmException ex) {
      throw CX_INVHASH_X.get(info, algorithm).cause(ex);
    } catch(final IllegalArgumentException | InvalidKeySpecException ex) {
      throw CX_KEYINV_X.get(info, ex);
    }
  }
}
