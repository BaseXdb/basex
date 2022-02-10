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
    final Iter iter = exprs[0].iter(qc);
    final FItem func = checkArity(exprs[1], 2, qc);

    Value sum = checkNoEmpty(iter.next());
    for(Item item; (item = qc.next(iter)) != null;) {
      sum = func.invoke(qc, info, sum, item);
    }
    return sum;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    if(expr1.seqType().zero()) throw EMPTYFOUND.get(info);

    // unroll fold
    if(expr2 instanceof Value) {
      final ExprList unroll = cc.unroll(expr1, true);
      if(unroll != null) {
        final FItem func = checkArity(expr2, 2, cc.qc);
        Expr expr = unroll.get(0);
        final long is = unroll.size();
        for(int i = 1; i < is; i++) {
          expr = new DynFuncCall(info, sc, func, expr, unroll.get(i)).optimize(cc);
        }
        return expr;
      }
    }

    final FuncType ft = expr2.funcType();
    if(ft != null) exprType.assign(ft.declType);
    return this;
  }
}
