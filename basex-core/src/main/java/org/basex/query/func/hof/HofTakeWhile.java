package org.basex.query.func.hof;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Implements the {@code hof:take-while($seq, $pred)} function.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Leo Woerteler
 */
public final class HofTakeWhile extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final FItem pred = checkArity(exprs[1], 1, qc);
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        final Item item = qc.next(iter);
        if(item != null && toBoolean(pred.invokeValue(qc, info, item), qc)) return item;
        return null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    final FItem pred = checkArity(exprs[1], 1, qc);
    final Iter iter = exprs[0].iter(qc);
    for(Item item; (item = qc.next(iter)) != null;) {
      if(!pred.invokeValue(qc, info, item).ebv(qc, info).bool(info)) break;
      vb.add(item);
    }
    return vb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr expr = exprs[0];
    final SeqType st = expr.seqType();
    if(st.zero()) return expr;

    exprType.assign(st.type, st.occ.union(Occ.ZERO));
    data(expr.data());
    return this;
  }
}
