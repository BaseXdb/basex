package org.basex.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * De- and encryption of passwords.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class Crypter {
  /** De- and encryption key. */
  private final String key = "BaseX";
  /** Bytes for de- and encryption. */
  private final byte[] salt = { (byte) 0xc9, (byte) 0xc9, (byte) 0xc9,
      (byte) 0xc9, (byte) 0xc9, (byte) 0xc9, (byte) 0xc9, (byte) 0xc9};
  /** Encryption cipher. */
  private Cipher encryptCipher;
  /** Decryption cipher. */
  private Cipher decryptCipher;
  /** Encoder. */
  private final sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
  /** Decoder. */
  private final sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();

  /** Standard constructor. */
  public Crypter() {
    init(key.toCharArray(), salt);
  }

  /**
   * Initiates the decryption mechanism.
   * @param pass char[]
   * @param s byte[]
   */
  public void init(final char[] pass, final byte[] s) {
    try {
      final PBEParameterSpec ps = new PBEParameterSpec(s, 20);
      final SecretKeyFactory kf = SecretKeyFactory.
      getInstance("PBEWithMD5AndDES");
      final SecretKey k = kf.generateSecret(new PBEKeySpec(pass));
      encryptCipher = Cipher.getInstance("PBEWithMD5AndDES/CBC/PKCS5Padding");
      encryptCipher.init(Cipher.ENCRYPT_MODE, k, ps);
      decryptCipher = Cipher.getInstance("PBEWithMD5AndDES/CBC/PKCS5Padding");
      decryptCipher.init(Cipher.DECRYPT_MODE, k, ps);
    } catch(final Exception ex) {
      throw new SecurityException("Could not initialize CryptoLibrary: "
          + ex.getMessage());
    }
  }

  /**
   * Encrypts a token.
   * @param tok token to be encrypted
   * @return encrypted string.
   */
  public synchronized String encrypt(final byte[] tok) {
    try {
      return encoder.encode(encryptCipher.doFinal(tok));
    } catch(final Exception ex) {
      throw new SecurityException("Could not encrypt: " + ex.getMessage());
    }
  }

  /**
   * Decrypts a string.
   * @param str Description of the Parameter
   * @return decrypted token.
   */
  public synchronized byte[] decrypt(final String str) {
    try {
      return decryptCipher.doFinal(decoder.decodeBuffer(str));
    } catch(final Exception ex) {
      throw new SecurityException("Could not decrypt: " + ex.getMessage());
    }
  }
}
