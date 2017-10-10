package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class MapForEach extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return toMap(exprs[0], qc).forEach(checkArity(exprs[1], 2, qc), qc, info);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Type t = exprs[1].seqType().type;
    if(t instanceof FuncType) seqType = SeqType.get(((FuncType) t).valueType.type, Occ.ZERO_MORE);
    return this;
  }
}
