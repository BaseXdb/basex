package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;

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
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Type t1 = exprs[0].seqType().type;
    if(t1 instanceof MapType) {
      final MapType mt1 = (MapType) t1;
      coerceFunc(1, cc, SeqType.ITEM_ZM, mt1.argTypes[0].type.seqType(), mt1.declType);
    }

    final Type t2 = exprs[1].seqType().type;
    if(t2 instanceof FuncType) exprType.assign(((FuncType) t2).declType.type);

    return this;
  }
}
