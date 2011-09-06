package org.basex.query.func;

import static org.basex.query.util.Err.*;

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
import org.basex.util.Token;

/**
 * EXPath Cryptographic Module.
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public class FNCrypto extends FuncCall {

  public FNCrypto(InputInfo ii, Function fd, Expr[] args) {
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
        return encrypt(checkStr(expr[0], ctx), checkStr(expr[1], ctx),
            checkStr(expr[2], ctx), checkStr(expr[3], ctx), ii);
      case DECRYPT:
      default:
        return super.item(ctx, ii);
    }
  }

  private Item encrypt(final byte[] in, final byte[] encType, final byte[] key,
      final byte[] algo, final InputInfo ii) throws QueryException {
    // encryption type must be 'symmetric' or 'asymmetric'
    if(!Token.eq(encType, Token.token("symmetric")) &&
        !Token.eq(encType, Token.token("asymmetric"))) {
      CRYPTOENCTYP.thrw(ii, encType);
    }

    byte[] keyCon = key;
//    keyCon = new byte[] { 0x0, 0x1, 0x2, 0x1, 0x2, 0x1, 0x2, 0x1 };
//    System.out.println(key.length);

    SecretKeySpec keyspec = new SecretKeySpec(keyCon, "DES");
    byte[] encrypted = null;

    try {

      // algorithm/mode/padding
      Cipher cip = Cipher.getInstance("DES/CBC/PKCS5Padding");
      cip.init(Cipher.ENCRYPT_MODE, keyspec);
      encrypted = new byte[cip.getOutputSize(in.length)];
      cip.doFinal(encrypted, cip.update(in, 0, in.length, encrypted, 0));

    } catch(NoSuchPaddingException e) {
      CRYPTONOPAD.thrw(ii, algo);
    } catch(BadPaddingException e) {
      CRYPTOBADPAD.thrw(ii, algo);
    } catch(NoSuchAlgorithmException e) {
      CRYPTOINVALGO.thrw(ii, algo);
    } catch(InvalidKeyException e) {
      CRYPTOKEYINV.thrw(ii, key);
    } catch(ShortBufferException e) {
      e.printStackTrace();
    } catch(IllegalBlockSizeException e) {
      CRYPTOILLBLO.thrw(ii, in);
    }

    return Str.get(encrypted);
  }

  private Item hmac(final byte[] msg, final byte[] key, final byte[] algo,
      final byte[] enc, final InputInfo ii) throws QueryException {

    // create hash value from input message
    Key k = new SecretKeySpec(key, Token.string(algo));
    byte[] hash = null;

    try {
      Mac mac = Mac.getInstance(Token.string(algo));
      mac.init(k);
      hash = mac.doFinal(msg);

    } catch(NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch(InvalidKeyException e) {
      e.printStackTrace();
    }

    // convert to specified encoding, base64 as a standard
    Item hmac = null;
    if(enc == null || Token.eq(enc, Token.token("base64")))
      hmac = new B64(hash);
    else if(Token.eq(Token.token("hex"), enc))
      hmac = new Hex(hash);
    else CRYPTOENC.thrw(ii, enc);

    return hmac;
  }
}
