package org.basex.query.func.hof;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Leo Woerteler
 */
public final class HofFoldLeft1 extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter input = exprs[0].iter(qc);
    final FItem action = toFunction(exprs[1], 2, qc);

    Value sum = checkNoEmpty(input.next());
    for(Item item; (item = qc.next(input)) != null;) {
      sum = action.invoke(qc, info, sum, item);
    }
    return sum;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = exprs[0], action = exprs[1];
    if(input.seqType().zero()) throw EMPTYFOUND.get(info);

    // unroll fold
    if(action instanceof Value) {
      final ExprList unroll = cc.unroll(input, true);
      if(unroll != null) {
        final FItem func = toFunction(action, 2, cc.qc);
        Expr expr = unroll.get(0);
        final long is = unroll.size();
        for(int i = 1; i < is; i++) {
          expr = new DynFuncCall(info, sc, func, expr, unroll.get(i)).optimize(cc);
        }
        return expr;
      }
    }

    final FuncType ft = action.funcType();
    if(ft != null) exprType.assign(ft.declType);
    return this;
  }
}
