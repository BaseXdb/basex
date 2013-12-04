package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.crypto.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * EXPath Cryptographic Module.
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
public final class FNCrypto extends StandardFunc {
  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNCrypto(final StaticContext sctx, final InputInfo ii, final Function f,
      final Expr... e) {
    super(sctx, ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
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

          if(arg6 instanceof AStr) arg6Str = true;
          else if(!(arg6 instanceof ANode)) throw Err.typeError(this, AtomType.STR, arg6);
        }
        return new DigitalSignature(ii).generateSignature(
            checkNode(expr[0].item(ctx, ii)), checkStr(expr[1], ctx),
            checkStr(expr[2], ctx), checkStr(expr[3], ctx),
            checkStr(expr[4], ctx), checkStr(expr[5], ctx),
            arg6Str ? arg6.string(ii) : Token.token(""),
            expr.length > 7 ? checkNode(expr[7].item(ctx, ii)) :
              expr.length == 7 && !arg6Str ? checkNode(expr[6].item(ctx, ii)) : null,
            ctx, info);
      case _CRYPTO_VALIDATE_SIGNATURE:
        return new DigitalSignature(ii).
            validateSignature(checkNode(expr[0].item(ctx, ii)));
      default:
        return super.item(ctx, ii);
    }
  }
}
