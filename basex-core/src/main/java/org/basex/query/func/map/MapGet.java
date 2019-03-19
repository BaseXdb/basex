package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Leo Woerteler
 */
public final class MapGet extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return toMap(exprs[0], qc).get(toAtomItem(exprs[1], qc), info);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Type type = exprs[0].seqType().type;
    // lookup may result in empty sequence
    if(type instanceof MapType) exprType.assign(((MapType) type).declType.occ.union(Occ.ZERO));
    return this;
  }
}
