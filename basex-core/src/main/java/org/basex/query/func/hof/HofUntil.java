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
 * @author BaseX Team 2005-23, BSD License
 * @author Leo Woerteler
 */
public final class HofUntil extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final FItem predicate = toFunction(arg(0), 1, qc), action = toFunction(arg(1), 1, qc);
    Value value = arg(2).value(qc);

    while(!toBoolean(predicate.invoke(qc, info, value).item(qc, info))) {
      value = action.invoke(qc, info, value);
    }
    return value;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr action = arg(1), zero = arg(2);
    final FuncType ft = action.funcType();
    if(ft != null) {
      final SeqType st = ft.declType.intersect(zero.seqType());
      if(st != null) exprType.assign(st);
    }
    return this;
  }
}
