package org.basex.util;

import javax.crypto.*;
import javax.crypto.spec.*;

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
  private sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
  /** Decoder. */
  private sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
  /** Used coding. */
  private String charset = "UTF16";

  /** Standard constructor. */
  public Crypter() {
    this.init(key.toCharArray(), salt);
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
   * Encrypts a string.
   * @param str Description of the Parameter
   * @return String the encrypted string.
   */
  public synchronized String encrypt(final String str) {
    try {
      final byte[] b = str.getBytes(this.charset);
      final byte[] enc = encryptCipher.doFinal(b);
      return encoder.encode(enc);
    } catch(final Exception ex) {
      throw new SecurityException("Could not encrypt: " + ex.getMessage());
    }
  }

  /**
   * Decrypts a string.
   * @param str Description of the Parameter
   * @return String the decrypted string.
   */
  public synchronized String decrypt(final String str) {
    try {
      final byte[] dec = decoder.decodeBuffer(str);
      final byte[] b = decryptCipher.doFinal(dec);
      return new String(b, this.charset);
    } catch(final Exception ex) {
      throw new SecurityException("Could not decrypt: " + ex.getMessage());
    }
  }
}
