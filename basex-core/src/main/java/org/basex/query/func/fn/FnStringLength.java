package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class FnStringLength extends ContextFn {
  @Override
  public Int item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] token;
    if(exprs.length == 0) {
      final Item item = ctxValue(qc).item(qc, info);
      if(item instanceof FItem) throw FISTRING_X.get(info, item.type);
      token = item == Empty.VALUE ? Token.EMPTY : item.string(info);
    } else {
      token = toEmptyToken(exprs[0], qc);
    }
    return Int.get(Token.length(token));
  }
}
