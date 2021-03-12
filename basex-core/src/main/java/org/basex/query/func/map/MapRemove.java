package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class MapRemove extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    XQMap map = toMap(exprs[0], qc);
    final Iter keys = exprs[1].iter(qc);
    for(Item item; (item = qc.next(keys)) != null;) map = map.delete(toAtomItem(item, qc), info);
    return map;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr expr1 = exprs[0];
    if(expr1 == XQMap.EMPTY) return expr1;

    final Type type = expr1.seqType().type;
    if(type instanceof MapType) exprType.assign(type);
    return this;
  }
}
