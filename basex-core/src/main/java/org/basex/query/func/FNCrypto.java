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
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNCrypto(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case _CRYPTO_HMAC:
        return new Encryption(ii).hmac(checkStr(exprs[0], qc), checkStr(exprs[1],
            qc), checkStr(exprs[2], qc),
            exprs.length == 4 ? checkStr(exprs[3], qc) : Token.EMPTY);
      case _CRYPTO_ENCRYPT:
        return new Encryption(ii).encryption(checkStr(exprs[0], qc),
            checkStr(exprs[1], qc), checkStr(exprs[2], qc),
            checkStr(exprs[3], qc), true);
      case _CRYPTO_DECRYPT:
        return new Encryption(ii).encryption(checkStr(exprs[0], qc),
            checkStr(exprs[1], qc), checkStr(exprs[2], qc),
            checkStr(exprs[3], qc), false);
      case _CRYPTO_GENERATE_SIGNATURE:
        // determine type of 7th argument
        Item arg6 = null;
        boolean arg6Str = false;
        if(exprs.length > 6) {
          arg6 = checkItem(exprs[6], qc);

          if(arg6 instanceof AStr) arg6Str = true;
          else if(!(arg6 instanceof ANode)) throw Err.castError(info, arg6, AtomType.STR);
        }
        return new DigitalSignature(ii).generateSignature(
            checkNode(exprs[0].item(qc, ii)), checkStr(exprs[1], qc),
            checkStr(exprs[2], qc), checkStr(exprs[3], qc),
            checkStr(exprs[4], qc), checkStr(exprs[5], qc),
            arg6Str ? arg6.string(ii) : Token.token(""),
            exprs.length > 7 ? checkNode(exprs[7].item(qc, ii)) :
              exprs.length == 7 && !arg6Str ? checkNode(exprs[6].item(qc, ii)) : null,
            qc, info);
      case _CRYPTO_VALIDATE_SIGNATURE:
        return new DigitalSignature(ii).
            validateSignature(checkNode(exprs[0].item(qc, ii)));
      default:
        return super.item(qc, ii);
    }
  }
}
