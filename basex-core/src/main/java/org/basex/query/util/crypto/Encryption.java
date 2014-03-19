package org.basex.query.util.crypto;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.security.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class encrypts and decrypts textual inputs.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public final class Encryption {
  /** Input info. */
  private final InputInfo info;
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

  static {
    ALGE.put("des", "8");
    ALGE.put("aes", "16");
    ALGN.put("des", "DES/CBC/PKCS5Padding");
    ALGN.put("aes", "AES/CBC/PKCS5Padding");
    ALGHMAC.put("md5", "hmacmd5");
    ALGHMAC.put("sha1", "hmacsha1");
    ALGHMAC.put("sha256", "hmacsha256");
    ALGHMAC.put("sha384", "hmacsha1");
    ALGHMAC.put("sha512", "hmacsha512");
  }

  /**
   * Constructor.
   *
   * @param ii input info
   */
  public Encryption(final InputInfo ii) {
    info = ii;
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
  public Str encryption(final byte[] in, final byte[] s, final byte[] k, final byte[] a,
      final boolean ec) throws QueryException {

    final boolean symmetric = eq(lc(s), SYM) || s.length == 0;
    final byte[] aa = a.length == 0 ? DES : a;
    final byte[] tivl = ALGE.get(lc(aa));
    if(!symmetric)
      throw CX_ENCTYP.get(info, ec);
    if(tivl == null)
      throw CX_INVALGO.get(info, s);
    // initialization vector length
    final int ivl = toInt(tivl);

    byte[] t = null;
    try {
      t = ec ? encrypt(in, k, aa, ivl) : decrypt(in, k, aa, ivl);
    } catch(final NoSuchPaddingException e) {
      throw CX_NOPAD.get(info, e);
    } catch(final BadPaddingException e) {
      throw CX_BADPAD.get(info, e);
    } catch(final InvalidKeyException e) {
      throw CX_KEYINV.get(info, e);
    } catch(final IllegalBlockSizeException e) {
      throw CX_ILLBLO.get(info, e);
    } catch(final GeneralSecurityException e) {
      throw CX_INVALGO.get(info, e);
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
  private static byte[] encrypt(final byte[] in, final byte[] k, final byte[] a, final int ivl)
      throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
      NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

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
  private static byte[] decrypt(final byte[] in, final byte[] k,
      final byte[] a, final int ivl) throws NoSuchAlgorithmException, NoSuchPaddingException,
      InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException,
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
  public Item hmac(final byte[] msg, final byte[] k, final byte[] a, final byte[] enc)
      throws QueryException {

    // create hash value from input message
    final Key key = new SecretKeySpec(k, string(a));

    final byte[] aa = a.length == 0 ? DEFA : a;
    if(!ALGHMAC.contains(lc(aa))) throw CX_INVHASH.get(info, aa);

    final boolean b64 = eq(lc(enc), BASE64) || enc.length == 0;
    if(!b64 && !eq(lc(enc), HEX))
      throw CX_ENC.get(info, enc);

    try {
      final Mac mac = Mac.getInstance(string(ALGHMAC.get(lc(aa))));
      mac.init(key);
      final byte[] hash = mac.doFinal(msg);
      // convert to specified encoding, base64 as a standard, else use hex
      return Str.get(b64 ? Base64.encode(hash) : hex(hash, true));
    } catch(final NoSuchAlgorithmException e) {
      throw CX_INVHASH.get(info, e);
    } catch(final InvalidKeyException e) {
      throw CX_KEYINV.get(info, e);
    }
  }
}
