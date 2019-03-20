package org.basex.query.func.hof;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Leo Woerteler
 */
public final class HofUntil extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final FItem pred = checkArity(exprs[0], 1, qc);
    final FItem func = checkArity(exprs[1], 1, qc);
    Value value = exprs[2].value(qc);

    while(!toBoolean(pred.invokeItem(qc, info, value))) {
      qc.checkStop();
      value = func.invokeValue(qc, info, value);
    }
    return value;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final FuncType ft = exprs[1].funcType();
    if(ft != null) {
      final SeqType st = ft.declType.intersect(exprs[2].seqType());
      if(st != null) exprType.assign(st);
    }
    return this;
  }
}
