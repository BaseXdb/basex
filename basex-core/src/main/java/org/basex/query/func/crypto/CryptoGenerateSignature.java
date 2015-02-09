package org.basex.query.func.crypto;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Lukas Kircher
 */
public final class CryptoGenerateSignature extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
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
  }
}
