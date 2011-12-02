package org.basex.query.func;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.ANode;
import org.basex.query.item.AtomType;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.util.Err;
import org.basex.query.util.crypto.DigitalSignature;
import org.basex.query.util.crypto.Encryption;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * EXPath Cryptographic Module.
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public class FNCrypto extends FuncCall {

  /**
   * Constructor.
   * @param ii input info
   * @param fd function
   * @param args function arguments
   */
  public FNCrypto(final InputInfo ii, final Function fd, final Expr[] args) {
    super(ii, fd, args);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    switch(def) {
      case _CRYPTO_HMAC:
        return new Encryption(ii).hmac(checkStr(expr[0], ctx), checkStr(expr[1],
            ctx), checkStr(expr[2], ctx),
            expr.length == 4 ? checkStr(expr[3], ctx) : Token.EMPTY);
      case _CRYPTO_ENCRYPT:
        return new Encryption(ii).encryption(checkStr(expr[0], ctx),
            checkStr(expr[1], ctx), checkStr(expr[2], ctx),
            checkStr(expr[3], ctx), true);
      case _CRYPTO_DECRYPT:
        return new Encryption(ii).encryption(checkStr(expr[0], ctx),
            checkStr(expr[1], ctx), checkStr(expr[2], ctx),
            checkStr(expr[3], ctx), false);
      case _CRYPTO_GENERATE_SIGNATURE:
        // determine type of 7th argument
        Item arg6 = null;
        boolean arg6Str = false;
        if(expr.length > 6) {
          arg6 = checkItem(expr[6], ctx);

          if(arg6 instanceof Str)arg6Str = true;
          else if(arg6 instanceof ANode);
          else Err.type(this, AtomType.STR, arg6);
        }
        return new DigitalSignature(ii).generateSignature(
            checkNode(expr[0].item(ctx, ii)), checkStr(expr[1], ctx),
            checkStr(expr[2], ctx), checkStr(expr[3], ctx),
            checkStr(expr[4], ctx), checkStr(expr[5], ctx),
            arg6Str ? arg6.string(ii) : Token.token(""),
            expr.length > 7 ? checkNode(expr[7].item(ctx, ii)) :
              expr.length == 7 && !arg6Str ? checkNode(expr[6].item(ctx, ii)) :
                null);
      case _CRYPTO_VALIDATE_SIGNATURE:
        return new DigitalSignature(ii).
            validateSignature(checkNode(expr[0].item(ctx, ii)));
      default:
        return super.item(ctx, ii);
    }
  }
}
