package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.B64;
import org.basex.query.item.Hex;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.util.InputInfo;

/**
 * EXPath Cryptographic Module.
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public class FNCrypto extends FuncCall {

  private static final byte[] SYM = token("symmetric");
  private static final byte[] ASYM = token("asymmetric");

  public FNCrypto(final InputInfo ii, final Function fd, final Expr[] args) {
    super(ii, fd, args);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(def) {
      case HMAC:
        return hmac(checkStr(expr[0], ctx), checkStr(expr[1], ctx),
            checkStr(expr[2], ctx), expr.length == 4 ? checkStr(expr[3], ctx)
                : null, ii);
      case ENCRYPT:
        return encryption(checkStr(expr[0], ctx), checkStr(expr[1], ctx),
            checkStr(expr[2], ctx), checkStr(expr[3], ctx), true, ii);
      case DECRYPT:
        return encryption(checkStr(expr[0], ctx), checkStr(expr[1], ctx),
            checkStr(expr[2], ctx), checkStr(expr[3], ctx), false, ii);
      default:
        return super.item(ctx, ii);
    }
  }

  private Item encryption(final byte[] in, final byte[] encrType,
      final byte[] key, final byte[] algo, final boolean encrypt,
      final InputInfo ii) throws QueryException {
 // encryption type must be 'symmetric' or 'asymmetric'
    if(!eq(encrType, SYM) && !eq(encrType, ASYM))
      if(encrypt) CRYPTOENCTYP.thrw(ii, encrType);
      else CRYPTODECTYP.thrw(ii, encrType);

    // [LK] symmetric/asymmetric encryption?

    // transformed input
    byte[] t = null;
    try {

      SecretKeySpec keyspec = new SecretKeySpec(key, "DES");
      // algorithm/mode/padding
      Cipher cip = Cipher.getInstance("DES/CBC/PKCS5Padding");

      // encrypt/decrypt
      if(encrypt)
        cip.init(Cipher.ENCRYPT_MODE, keyspec);
      else
        cip.init(Cipher.DECRYPT_MODE, keyspec);

      t = new byte[cip.getOutputSize(in.length)];
      cip.doFinal(t, cip.update(in, 0, in.length, t, 0));

    } catch(NoSuchPaddingException e) {
      CRYPTONOPAD.thrw(ii, algo);
    } catch(BadPaddingException e) {
      CRYPTOBADPAD.thrw(ii, algo);
    } catch(NoSuchAlgorithmException e) {
      CRYPTOINVALGO.thrw(ii, algo);
    } catch(InvalidKeyException e) {
      CRYPTOKEYINV.thrw(ii, key);
      // [LK] how to treat this one?
    } catch(ShortBufferException e) {
      e.printStackTrace();
    } catch(IllegalBlockSizeException e) {
      CRYPTOILLBLO.thrw(ii, in);
    }

    return Str.get(t);
  }

  private Item hmac(final byte[] msg, final byte[] key, final byte[] algo,
      final byte[] enc, final InputInfo ii) throws QueryException {

    // create hash value from input message
    Key k = new SecretKeySpec(key, string(algo));
    byte[] hash = null;

    try {
      Mac mac = Mac.getInstance(string(algo));
      mac.init(k);
      hash = mac.doFinal(msg);

    } catch(NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch(InvalidKeyException e) {
      e.printStackTrace();
    }

    // convert to specified encoding, base64 as a standard
    Item hmac = null;
    if(enc == null || eq(enc, token("base64")))
      hmac = new B64(hash);
    else if(eq(token("hex"), enc))
      hmac = new Hex(hash);
    else CRYPTOENC.thrw(ii, enc);

    return Str.get(hmac.toString());
  }
}
