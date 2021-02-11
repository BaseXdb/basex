package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
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
public final class MapEntry extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return XQMap.EMPTY.put(toAtomItem(exprs[0], qc), exprs[1].value(qc), info);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final AtomType type1 = exprs[0].seqType().type.atomic();
    if(type1 != null) exprType.assign(MapType.get(type1, exprs[1].seqType()));
    return this;
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) {
    // do not simplify type of key
  }
}
