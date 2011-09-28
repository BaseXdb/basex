package org.basex.query.util.crypto;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.util.Base64;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * This class encrypts and decrypts textual inputs.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class Encryption {

  /** Token. */
  private static final byte[] SYM = token("symmetric");
  /** Token. */
  private static final byte[] ASYM = token("asymmetric");
  /** Token. */
  private static final byte[] BASE64 = token("base64");
  /** Token. */
  private static final byte[] HEX = token("hex");
  /** Input info. */
  private final InputInfo input;

  /**
   * Constructor.
   *
   * @param ii input info
   */
  public Encryption(final InputInfo ii) {
    input = ii;
  }

  /**
   * Encrypts or decrypts the given input.
   * @param in input
   * @param type encryption type
   * @param key secret key
   * @param algorithm encryption algorithm
   * @param encrypt encrypt or decrypt
   * @return encrypted or decrypted input
   * @throws QueryException query exception
   */
  public Item encryption(final byte[] in, final byte[] type,
      final byte[] key, final byte[] algorithm, final boolean encrypt)
          throws QueryException {

    final boolean symmetric = eq(type, SYM);
    // encryption type must be 'symmetric' or 'asymmetric', error message
    // dependent on encryption/decryption
    if(!symmetric && !eq(type, ASYM))
      if(encrypt) CRYPTOENCTYP.thrw(input, type);
      else CRYPTODECTYP.thrw(input, type);

    // asymmetric encryption not yet supported
    if(!symmetric)
      CRYPTONOTSUPP.thrw(input, "asymmetric encryption");

    byte[] t = null;

    try {

      if(encrypt)
        t = encrypt(in, key, algorithm);
      else
        t = decrypt(in, key, algorithm);

    } catch(NoSuchPaddingException e) {
      e.printStackTrace();
      CRYPTONOPAD.thrw(input, e);
    } catch(BadPaddingException e) {
      e.printStackTrace();
      CRYPTOBADPAD.thrw(input, e);
    } catch(NoSuchAlgorithmException e) {
      e.printStackTrace();
      CRYPTOINVALGO.thrw(input, e);
    } catch(InvalidKeyException e) {
      e.printStackTrace();
      CRYPTOKEYINV.thrw(input, e);
    } catch(IllegalBlockSizeException e) {
      e.printStackTrace();
      CRYPTOILLBLO.thrw(input, e);
    } catch(InvalidAlgorithmParameterException e) {
      e.printStackTrace();
      CRYPTONOTSUPP.thrw(input, "invalid algorithm parameter");
    }

    return Str.get(t);
  }

  /**
   * Encrypts the given input data.
   *
   * @param in input data to encrypt
   * @param key key
   * @param algorithm encryption algorithm
   * @return encrypted input data
   * @throws InvalidKeyException ex
   * @throws InvalidAlgorithmParameterException ex
   * @throws NoSuchAlgorithmException ex
   * @throws NoSuchPaddingException ex
   * @throws IllegalBlockSizeException ex
   * @throws BadPaddingException ex
   */
  public byte[] encrypt(final byte[] in, final byte[] key,
      final byte[] algorithm) throws InvalidKeyException,
      InvalidAlgorithmParameterException, NoSuchAlgorithmException,
      NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

    final String algo = string(algorithm);
    final SecretKeySpec keySpec = new SecretKeySpec(key, algo.substring(0, 3));
    final Cipher cipher = Cipher.getInstance(algo);

    // generate random iv
    byte[] iv = new byte[8];
    // create new random iv if encrypting
    final SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
    rand.nextBytes(iv);
    final IvParameterSpec ivspec = new IvParameterSpec(iv);
    System.out.println("random iv: " + string(iv));

    // encrypt/decrypt
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivspec);
    final byte[] t = cipher.doFinal(in);

    return concat(iv, t);
  }

  /**
   * Decrypts the given input data.
   *
   * @param in data to decrypt
   * @param key secret key
   * @param algorithm encryption algorithm
   * @return decrypted data
   * @throws NoSuchAlgorithmException ex
   * @throws NoSuchPaddingException ex
   * @throws InvalidKeyException ex
   * @throws InvalidAlgorithmParameterException ex
   * @throws IllegalBlockSizeException ex
   * @throws BadPaddingException ex
   */
  public byte[] decrypt(final byte[] in, final byte[] key,
      final byte[] algorithm) throws NoSuchAlgorithmException,
      NoSuchPaddingException, InvalidKeyException,
      InvalidAlgorithmParameterException,
      IllegalBlockSizeException, BadPaddingException {

    final String algo = string(algorithm);
    final SecretKeySpec keySpec = new SecretKeySpec(key, algo.substring(0, 3));
    final Cipher cipher = Cipher.getInstance(algo);

    // extract iv from message beginning
    byte[] iv = substring(in, 0, 8);
    final IvParameterSpec ivspec = new IvParameterSpec(iv);
    System.out.println("extracted iv: " + string(iv) + "\n");

    cipher.init(Cipher.DECRYPT_MODE, keySpec, ivspec);
    return cipher.doFinal(substring(in, 8, in.length));
  }

  /**
   * Creates a message authentication code (MAC) for the given input.
   * @param msg input
   * @param key secret key
   * @param algo encryption algorithm
   * @param enc encoding
   * @return MAC
   * @throws QueryException query exception
   */
  public Item hmac(final byte[] msg, final byte[] key, final byte[] algo,
      final byte[] enc) throws QueryException {

    // create hash value from input message
    final Key k = new SecretKeySpec(key, string(algo));
    byte[] hash = null;

    try {
      Mac mac = Mac.getInstance(string(algo));
      mac.init(k);
      hash = mac.doFinal(msg);

    } catch(NoSuchAlgorithmException e) {
      CRYPTOINVHASH.thrw(input, algo);
    } catch(InvalidKeyException e) {
      CRYPTOKEYINV.thrw(input, key);
    }

    // convert to specified encoding, base64 as a standard
    Str hmac = null;
    if(enc == null || eq(enc, BASE64))
      hmac = Str.get(Base64.encode(hash));
    else if(eq(HEX, enc))
      hmac = Str.get(Token.hex(hash, true));
    else CRYPTOENC.thrw(input, enc);

    return Str.get(hmac.toString());
  }
}
