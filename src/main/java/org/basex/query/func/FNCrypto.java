package org.basex.query.func;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.util.crypto.DigitalSignature;
import org.basex.query.util.crypto.Encryption;
import org.basex.util.InputInfo;

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
      case HMAC:
        return new Encryption(ii).hmac(checkStr(expr[0], ctx), checkStr(expr[1],
            ctx), checkStr(expr[2], ctx),
            expr.length == 4 ? checkStr(expr[3], ctx) : null);
      case ENCRYPT:
        return new Encryption(ii).encryption(checkStr(expr[0], ctx),
            checkStr(expr[1], ctx), checkStr(expr[2], ctx),
            checkStr(expr[3], ctx), true);
      case DECRYPT:
        return new Encryption(ii).encryption(checkStr(expr[0], ctx),
            checkStr(expr[1], ctx), checkStr(expr[2], ctx),
            checkStr(expr[3], ctx), false);
      case GENSIG:
        return new DigitalSignature(ii).generateSignature(
            checkNode(expr[0].item(ctx, ii)), checkStr(expr[1], ctx),
            checkStr(expr[2], ctx), checkStr(expr[3], ctx),
            checkStr(expr[4], ctx), checkStr(expr[5], ctx),
            expr.length > 6 ? checkNode(expr[6].item(ctx, ii)) : null);
      case VALSIG:
        return new DigitalSignature(ii).
            validateSignature(checkNode(expr[0].item(ctx, ii)));

      default:
        return super.item(ctx, ii);
    }
  }
}
