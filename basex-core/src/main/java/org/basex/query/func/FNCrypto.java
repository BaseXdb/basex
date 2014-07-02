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
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public final class FNCrypto extends StandardFunc {
  /**
   * Constructor.
   * @param sctx static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNCrypto(final StaticContext sctx, final InputInfo info, final Function func,
      final Expr... args) {
    super(sctx, info, func, args);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(func) {
      case _CRYPTO_HMAC:
        return new Encryption(ii).hmac(checkStr(exprs[0], ctx), checkStr(exprs[1],
            ctx), checkStr(exprs[2], ctx),
            exprs.length == 4 ? checkStr(exprs[3], ctx) : Token.EMPTY);
      case _CRYPTO_ENCRYPT:
        return new Encryption(ii).encryption(checkStr(exprs[0], ctx),
            checkStr(exprs[1], ctx), checkStr(exprs[2], ctx),
            checkStr(exprs[3], ctx), true);
      case _CRYPTO_DECRYPT:
        return new Encryption(ii).encryption(checkStr(exprs[0], ctx),
            checkStr(exprs[1], ctx), checkStr(exprs[2], ctx),
            checkStr(exprs[3], ctx), false);
      case _CRYPTO_GENERATE_SIGNATURE:
        // determine type of 7th argument
        Item arg6 = null;
        boolean arg6Str = false;
        if(exprs.length > 6) {
          arg6 = checkItem(exprs[6], ctx);

          if(arg6 instanceof AStr) arg6Str = true;
          else if(!(arg6 instanceof ANode)) throw Err.typeError(this, AtomType.STR, arg6);
        }
        return new DigitalSignature(ii).generateSignature(
            checkNode(exprs[0].item(ctx, ii)), checkStr(exprs[1], ctx),
            checkStr(exprs[2], ctx), checkStr(exprs[3], ctx),
            checkStr(exprs[4], ctx), checkStr(exprs[5], ctx),
            arg6Str ? arg6.string(ii) : Token.token(""),
            exprs.length > 7 ? checkNode(exprs[7].item(ctx, ii)) :
              exprs.length == 7 && !arg6Str ? checkNode(exprs[6].item(ctx, ii)) : null,
            ctx, info);
      case _CRYPTO_VALIDATE_SIGNATURE:
        return new DigitalSignature(ii).
            validateSignature(checkNode(exprs[0].item(ctx, ii)));
      default:
        return super.item(ctx, ii);
    }
  }
}
