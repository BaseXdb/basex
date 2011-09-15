package org.basex.query.util.crypto;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
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
   * @param encryptionType encryption type
   * @param key secret key
   * @param algorithm encryption algorithm
   * @param encrypt encrypt or decrypt
   * @return encrypted or decrypted input
   * @throws QueryException query exception
   */
  public Item encryption(final byte[] in, final byte[] encryptionType,
      final byte[] key, final byte[] algorithm, final boolean encrypt)
          throws QueryException {
    
    boolean symmetric = eq(encryptionType, SYM); 
    // encryption type must be 'symmetric' or 'asymmetric'
    if(!symmetric && !eq(encryptionType, ASYM))
      if(encrypt) CRYPTOENCTYP.thrw(input, encryptionType);
      else CRYPTODECTYP.thrw(input, encryptionType);

    // transformed input
    byte[] t = null;
    final String a = string(algorithm);
    try {

      if(symmetric) {
        SecretKeySpec keyspec = new SecretKeySpec(key, "DES");
        IvParameterSpec iv = new IvParameterSpec(new byte[key.length]);
        // algorithm/mode/padding
        Cipher cip = Cipher.getInstance(a);
        
        // encrypt/decrypt
        if(encrypt)
          cip.init(Cipher.ENCRYPT_MODE, keyspec, iv);
        else
          cip.init(Cipher.DECRYPT_MODE, keyspec, iv);
        
        t = new byte[cip.getOutputSize(in.length)];
        cip.doFinal(t, cip.update(in, 0, in.length, t, 0));
      
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
      // [LK] how to treat this one?
    } catch(ShortBufferException e) {
      e.printStackTrace();
      CRYPTONOTSUPP.thrw(input, "short buffer");
    } catch(IllegalBlockSizeException e) {
      e.printStackTrace();
      CRYPTOILLBLO.thrw(input, e);
   // [LK] how to treat this one?
    } catch(InvalidAlgorithmParameterException e) {
      e.printStackTrace();
      CRYPTONOTSUPP.thrw(input, "invalid algorithm parameter");
    }

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
    Key k = new SecretKeySpec(key, string(algo));
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
