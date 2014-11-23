package org.basex.query.func.hof;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class HofFoldLeft1 extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final FItem f = checkArity(exprs[1], 2, qc);
    final Iter iter = exprs[0].iter(qc);

    Value sum = checkNoEmpty(iter.next());
    for(Item it; (it = iter.next()) != null;) sum = f.invokeValue(qc, info, sum, it);
    return sum;
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) throws QueryException {
    if(allAreValues() && exprs[0].size() <= FnForEach.UNROLL_LIMIT) {
      qc.compInfo(QueryText.OPTUNROLL, this);
      final Value seq = (Value) exprs[0];
      if(seq.isEmpty()) throw EMPTYFOUND.get(info);
      final FItem f = checkArity(exprs[1], 2, qc);
      Expr e = seq.itemAt(0);
      final long is = seq.size();
      for(int i = 1; i < is; i++)
        e = new DynFuncCall(info, sc, false, f, e, seq.itemAt(i)).optimize(qc, scp);
      return e;
    }
    return this;
  }
}
