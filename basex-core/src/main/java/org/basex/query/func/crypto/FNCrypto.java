package org.basex.query.func.crypto;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.func.*;
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
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case _CRYPTO_HMAC:
        return new Encryption(ii).hmac(toToken(exprs[0], qc), toToken(exprs[1],
            qc), toToken(exprs[2], qc),
            exprs.length == 4 ? toToken(exprs[3], qc) : Token.EMPTY);
      case _CRYPTO_ENCRYPT:
        return new Encryption(ii).encryption(toToken(exprs[0], qc),
            toToken(exprs[1], qc), toToken(exprs[2], qc), toToken(exprs[3], qc), true);
      case _CRYPTO_DECRYPT:
        return new Encryption(ii).encryption(toToken(exprs[0], qc),
            toToken(exprs[1], qc), toToken(exprs[2], qc), toToken(exprs[3], qc), false);
      case _CRYPTO_GENERATE_SIGNATURE:
        // determine type of 7th argument
        Item arg6 = null;
        boolean arg6Str = false;
        if(exprs.length > 6) {
          arg6 = toItem(exprs[6], qc);
          if(arg6 instanceof AStr) arg6Str = true;
          else if(!(arg6 instanceof ANode)) throw castError(info, arg6, AtomType.STR);
        }
        return new DigitalSignature(ii).generateSignature(
            toNode(exprs[0], qc), toToken(exprs[1], qc),
            toToken(exprs[2], qc), toToken(exprs[3], qc),
            toToken(exprs[4], qc), toToken(exprs[5], qc),
            arg6Str ? arg6.string(ii) : Token.token(""),
            exprs.length > 7 ? toNode(exprs[7], qc) :
              exprs.length == 7 && !arg6Str ? toNode(exprs[6], qc) : null,
            qc, info);
      case _CRYPTO_VALIDATE_SIGNATURE:
        return new DigitalSignature(ii).validateSignature(toNode(exprs[0], qc));
      default:
        return super.item(qc, ii);
    }
  }
}
