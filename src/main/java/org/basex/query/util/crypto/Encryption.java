package org.basex.query.util.crypto;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

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
  /** Supported asymmetric encryption algorithm. */
  private static final byte[] RSA = token("RSA");
  /** Supported encryption algorithms. */
  private static final byte[][] ALG =
    {
      token("DES"),
      RSA,
      /*
      token("DES3"),
      token("AES")
      */
    };

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
   * @param s encryption type
   * @param k secret key
   * @param a encryption algorithm
   * @param ec encrypt or decrypt
   * @return encrypted or decrypted input
   * @throws QueryException query exception
   */
  public Item encryption(final byte[] in, final byte[] s,
      final byte[] k, final byte[] a, final boolean ec)
          throws QueryException {

    final boolean symmetric = eq(s, SYM);
    // encryption type must be 'symmetric' or 'asymmetric', error message
    // dependent on encryption/decryption
    if(!symmetric && !eq(s, ASYM))
      if(ec) CRYPTOENCTYP.thrw(input, s);
      else CRYPTODECTYP.thrw(input, s);

    // check given algorithm and combination with encryption type (symmetric...)
    final byte[] a3 = substring(a, 0, 3);
    if(!eq(a3, ALG))
      CRYPTOINVALGO.thrw(input, a3);
    if(symmetric && eq(a3, RSA))
      CRYPTOSYMERR.thrw(input, a3);

    byte[] t = null;
    try {

      if(ec)
        t = encrypt(in, k, string(a), symmetric);
      else
        t = decrypt(in, k, string(a), symmetric);

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
    } catch(InvalidKeySpecException e) {
      e.printStackTrace();
    }

    return Str.get(t);
  }

  /**
   * Encrypts the given input data.
   *
   * @param in input data to encrypt
   * @param k key
   * @param a encryption algorithm
   * @param s symmetric encryption
   * @return encrypted input data
   * @throws InvalidKeyException ex
   * @throws InvalidAlgorithmParameterException ex
   * @throws NoSuchAlgorithmException ex
   * @throws NoSuchPaddingException ex
   * @throws IllegalBlockSizeException ex
   * @throws BadPaddingException ex
   * @throws InvalidKeySpecException ex
   * @throws QueryException ex
   */
  public byte[] encrypt(final byte[] in, final byte[] k,
      final String a, final boolean s) throws InvalidKeyException,
      InvalidAlgorithmParameterException, NoSuchAlgorithmException,
      NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
      InvalidKeySpecException, QueryException {

    final Cipher cipher = Cipher.getInstance(a);

    if(s) {
      final SecretKeySpec kspec = new SecretKeySpec(k, a.substring(0, 3));
      // generate random iv
      byte[] iv = new byte[8];
      // create new random iv if encrypting
      final SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
      rand.nextBytes(iv);
      final IvParameterSpec ivspec = new IvParameterSpec(iv);
//    System.out.println("random iv: " + string(iv));

      // encrypt/decrypt
      cipher.init(Cipher.ENCRYPT_MODE, kspec, ivspec);
      final byte[] t = cipher.doFinal(in);
      return concat(iv, t);

    }

    CRYPTONOTSUPP.thrw(input, s);

    final KeyFactory kfac = KeyFactory.getInstance(a);
    final KeySpec kspec = new PKCS8EncodedKeySpec(k);
    final RSAPrivateKey key = (RSAPrivateKey) kfac.generatePrivate(kspec);

    // asymmetric encryption
    cipher.init(Cipher.ENCRYPT_MODE, key);
    return cipher.doFinal(in);
  }

  /**
   * Decrypts the given input data.
   *
   * @param in data to decrypt
   * @param k secret key
   * @param a encryption algorithm
   * @param s symmetric encryption
   * @return decrypted data
   * @throws NoSuchAlgorithmException ex
   * @throws NoSuchPaddingException ex
   * @throws InvalidKeyException ex
   * @throws InvalidAlgorithmParameterException ex
   * @throws IllegalBlockSizeException ex
   * @throws BadPaddingException ex
   */
  public byte[] decrypt(final byte[] in, final byte[] k,
      final String a, final boolean s) throws NoSuchAlgorithmException,
      NoSuchPaddingException, InvalidKeyException,
      InvalidAlgorithmParameterException, IllegalBlockSizeException,
      BadPaddingException {

    final SecretKeySpec keySpec = new SecretKeySpec(k, a.substring(0, 3));
    final Cipher cipher = Cipher.getInstance(a);

    // extract iv from message beginning
    byte[] iv = substring(in, 0, 8);
    final IvParameterSpec ivspec = new IvParameterSpec(iv);
//    System.out.println("extracted iv: " + string(iv) + "\n");

    cipher.init(Cipher.DECRYPT_MODE, keySpec, ivspec);
    return cipher.doFinal(substring(in, 8, in.length));
  }

  /**
   * Creates a message authentication code (MAC) for the given input.
   * @param msg input
   * @param k secret key
   * @param a encryption algorithm
   * @param enc encoding
   * @return MAC
   * @throws QueryException query exception
   */
  public Item hmac(final byte[] msg, final byte[] k, final byte[] a,
      final byte[] enc) throws QueryException {

    // create hash value from input message
    final Key key = new SecretKeySpec(k, string(a));
    byte[] hash = null;

    try {
      Mac mac = Mac.getInstance(string(a));
      mac.init(key);
      hash = mac.doFinal(msg);

    } catch(NoSuchAlgorithmException e) {
      CRYPTOINVHASH.thrw(input, a);
    } catch(InvalidKeyException e) {
      CRYPTOKEYINV.thrw(input, k);
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
