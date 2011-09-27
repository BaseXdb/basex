package org.basex.query.util.crypto;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
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

    // transformed input
    byte[] t = null;
    final String algo = string(algorithm);
    final String ka = algo.substring(0, 3);

//    try {
//
//      final Key k = KeyGenerator.getInstance(ka).generateKey();
//      final Cipher cipher = Cipher.getInstance(algo);
//      t = in;
//
//      if(encrypt) {
//        cipher.init(Cipher.ENCRYPT_MODE, k);
//        cipher.doFinal(t);
//
//      } else {
//        cipher.init(Cipher.DECRYPT_MODE, k);
//        t = cipher.doFinal(t);
//      }
//
//    } catch (NoSuchPaddingException e) {
//      e.printStackTrace();
//    } catch(NoSuchAlgorithmException e) {
//      e.printStackTrace();
//    } catch(InvalidKeyException e) {
//      e.printStackTrace();
//    } catch(IllegalBlockSizeException e) {
//      e.printStackTrace();
//    } catch(BadPaddingException e) {
//      e.printStackTrace();
//    }

    try {

      if(symmetric) {
        final SecretKeySpec keySpec = new SecretKeySpec(key, ka);
        // TODO random IV?
        final byte[] iVector = new byte[key.length];
        new Random().nextBytes(iVector);
        final IvParameterSpec iv = new IvParameterSpec(iVector);
//        IvParameterSpec iv = new IvParameterSpec(new byte[key.length]);
        final Cipher cipher = Cipher.getInstance(algo);

        // encrypt/decrypt
        if(encrypt)
          cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
        else
          cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);

        t = new byte[cipher.getOutputSize(in.length)];
        cipher.doFinal(t, cipher.update(in, 0, in.length, t, 0));

        // asymmetric encryption
      } else {
        CRYPTONOTSUPP.thrw(input, "asymmetric encryption");
      }

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
      // TODO how to treat this one?
    } catch(ShortBufferException e) {
      e.printStackTrace();
      CRYPTONOTSUPP.thrw(input, "short buffer");
    } catch(IllegalBlockSizeException e) {
      e.printStackTrace();
      CRYPTOILLBLO.thrw(input, e);
   // TODO how to treat this one?
    } catch(InvalidAlgorithmParameterException e) {
      e.printStackTrace();
      CRYPTONOTSUPP.thrw(input, "invalid algorithm parameter");
    }

    // TODO remove padding leftovers?
    return Str.get(t);
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
