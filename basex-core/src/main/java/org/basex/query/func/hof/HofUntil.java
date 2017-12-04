package org.basex.query.func.hof;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class HofUntil extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final FItem pred = checkArity(exprs[0], 1, qc), fun = checkArity(exprs[1], 1, qc);
    Value v = exprs[2].value(qc);
    while(!toBoolean(pred.invokeItem(qc, info, v))) {
      qc.checkStop();
      v = fun.invokeValue(qc, info, v);
    }
    return v;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Type t = exprs[1].seqType().type;
    if(t instanceof FuncType) {
      final SeqType dt = ((FuncType) t).declType, it = dt.intersect(exprs[2].seqType());
      if(it != null) exprType.assign(it);
    }
    return this;
  }
}
