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
import org.basex.util.hash.TokenMap;

/**
 * This class encrypts and decrypts textual inputs.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class Encryption {

  /** Input info. */
  private final InputInfo input;
  /** Token. */
  private static final byte[] SYM = token("symmetric");
  /** Token. */
  private static final byte[] BASE64 = token("base64");
  /** Token. */
  private static final byte[] HEX = token("hex");
  /** Supported encryption algorithms, mapped to correct IV lengths. */
  private static final TokenMap ALGE = new TokenMap();
  /** Exact encryption algorithm JAVA names. */
  private static final TokenMap ALGN = new TokenMap();
  /** Supported HMAC algorithms. */
  private static final TokenMap ALGHMAC = new TokenMap();
  /** Default hash algorithm. */
  private static final byte[] DEFA = token("md5");
  /** DES encryption token. */
  private static final byte[] DES = token("des");
  /** AES encryption token. */
  private static final byte[] AES = token("aes");

  static {
    ALGE.add(DES, token("8"));
    ALGE.add(AES, token("16"));
    ALGN.add(DES, token("DES/CBC/PKCS5Padding"));
    ALGN.add(AES, token("AES/CBC/PKCS5Padding"));
    ALGHMAC.add(DEFA, token("hmacmd5"));
    ALGHMAC.add(token("sha1"), token("hmacsha1"));
    ALGHMAC.add(token("sha256"), token("hmacsha256"));
    ALGHMAC.add(token("sha384"), token("hmacsha1"));
    ALGHMAC.add(token("sha512"), token("hmacsha512"));
    /*
    */
  }

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
  public Str encryption(final byte[] in, final byte[] s,
      final byte[] k, final byte[] a, final boolean ec)
          throws QueryException {

    final boolean symmetric = eq(lc(s), SYM) || s.length == 0;
    final byte[] aa = a.length == 0 ? DES : a;
    final byte[] tivl = ALGE.get(lc(aa));
    if(!symmetric)
      CRYPTOENCTYP.thrw(input, ec);
    if(tivl == null)
      CRYPTOINVALGO.thrw(input, s);
    // initialization vector length
    final int ivl = toInt(tivl);

    byte[] t = null;
    try {

      if(ec)
        t = encrypt(in, k, aa, ivl);
      else
        t = decrypt(in, k, aa, ivl);

    } catch(final NoSuchPaddingException e) {
      CRYPTONOPAD.thrw(input, e);
    } catch(final BadPaddingException e) {
      CRYPTOBADPAD.thrw(input, e);
    } catch(final NoSuchAlgorithmException e) {
      CRYPTOINVALGO.thrw(input, e);
    } catch(final InvalidKeyException e) {
      CRYPTOKEYINV.thrw(input, e);
    } catch(final IllegalBlockSizeException e) {
      CRYPTOILLBLO.thrw(input, e);
    } catch(final InvalidAlgorithmParameterException e) {
      CRYPTOINVALGO.thrw(input, e);
    }

    return Str.get(t);
  }

  /**
   * Encrypts the given input data.
   *
   * @param in input data to encrypt
   * @param k key
   * @param a encryption algorithm
   * @param ivl initialization vector length
   * @return encrypted input data
   * @throws InvalidKeyException ex
   * @throws InvalidAlgorithmParameterException ex
   * @throws NoSuchAlgorithmException ex
   * @throws NoSuchPaddingException ex
   * @throws IllegalBlockSizeException ex
   * @throws BadPaddingException ex
   */
  byte[] encrypt(final byte[] in, final byte[] k,
                 final byte[] a, final int ivl)
          throws InvalidKeyException, InvalidAlgorithmParameterException,
          NoSuchAlgorithmException, NoSuchPaddingException,
          IllegalBlockSizeException, BadPaddingException {

    final Cipher cipher = Cipher.getInstance(string(ALGN.get(lc(a))));
    final SecretKeySpec kspec = new SecretKeySpec(k, string(a));
    // generate random iv. random iv is necessary to make the encryption of a
    // string look different every time it is encrypted.
    final byte[] iv = new byte[ivl];
    // create new random iv if encrypting
    final SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
    rand.nextBytes(iv);
    final IvParameterSpec ivspec = new IvParameterSpec(iv);

    // encrypt/decrypt
    cipher.init(Cipher.ENCRYPT_MODE, kspec, ivspec);
    final byte[] t = cipher.doFinal(in);
    // initialization vector is appended to the message for later decryption
    return concat(iv, t);
  }

  /**
   * Decrypts the given input data.
   *
   * @param in data to decrypt
   * @param k secret key
   * @param a encryption algorithm
   * @param ivl initialization vector length
   * @return decrypted data
   * @throws NoSuchAlgorithmException ex
   * @throws NoSuchPaddingException ex
   * @throws InvalidKeyException ex
   * @throws InvalidAlgorithmParameterException ex
   * @throws IllegalBlockSizeException ex
   * @throws BadPaddingException ex
   */
  byte[] decrypt(final byte[] in, final byte[] k,
                 final byte[] a, final int ivl)
          throws NoSuchAlgorithmException,
      NoSuchPaddingException, InvalidKeyException,
      InvalidAlgorithmParameterException, IllegalBlockSizeException,
      BadPaddingException {

    final SecretKeySpec keySpec = new SecretKeySpec(k, string(a));
    final Cipher cipher = Cipher.getInstance(string(ALGN.get(lc(a))));

    // extract iv from message beginning
    final byte[] iv = substring(in, 0, ivl);
    final IvParameterSpec ivspec = new IvParameterSpec(iv);
    cipher.init(Cipher.DECRYPT_MODE, keySpec, ivspec);
    return cipher.doFinal(substring(in, ivl, in.length));
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

    final byte[] aa = a.length == 0 ? DEFA : a;
    if(ALGHMAC.id(lc(aa)) == 0)
      CRYPTOINVHASH.thrw(input, aa);

    final boolean b64 = eq(lc(enc), BASE64) || enc.length == 0;
    if(!b64 && !eq(lc(enc), HEX))
      CRYPTOENC.thrw(input, enc);

    try {
      final Mac mac = Mac.getInstance(string(ALGHMAC.get(lc(aa))));
      mac.init(key);
      hash = mac.doFinal(msg);

    } catch(final NoSuchAlgorithmException e) {
      CRYPTOINVHASH.thrw(input, e);
    } catch(final InvalidKeyException e) {
      CRYPTOKEYINV.thrw(input, e);
    }

    // convert to specified encoding, base64 as a standard, else use hex
    return Str.get(b64 ? Base64.encode(hash) : hex(hash, true));
  }
}
