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
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class MapRemove extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    XQMap map = toMap(arg(0), qc);
    final Iter keys = arg(1).iter(qc);

    for(Item item; (item = qc.next(keys)) != null;) {
      map = map.delete(toAtomItem(item, qc));
    }
    return map;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr map = arg(0);
    if(map == XQMap.empty()) return map;

    final Type type = map.seqType().type;
    if(type instanceof MapType) exprType.assign(type);
    return this;
  }
}
