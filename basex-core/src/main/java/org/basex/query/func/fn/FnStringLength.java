package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnStringLength extends ContextFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] tok;
    if(exprs.length == 0) {
      final Item it = ctxValue(qc).item(qc, info);
      if(it instanceof FItem) throw FISTRING_X.get(info, it.type);
      tok = it == null ? Token.EMPTY : it.string(info);
    } else {
      tok = toEmptyToken(exprs[0], qc);
    }
    return Int.get(Token.length(tok));
  }
}
