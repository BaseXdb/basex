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
 * @author BaseX Team 2005-17, BSD License
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
        final Item it = qc.next(iter);
        if(it != null && toBoolean(pred.invokeValue(qc, info, it), qc)) return it;
        return null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    final FItem pred = checkArity(exprs[1], 1, qc);
    final Iter iter = exprs[0].iter(qc);
    for(Item it; (it = qc.next(iter)) != null;) {
      if(!pred.invokeValue(qc, info, it).ebv(qc, info).bool(info)) break;
      vb.add(it);
    }
    return vb.value();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr ex = exprs[0];
    final SeqType st = ex.seqType();
    if(st.zero()) return ex;
    exprType.assign(st.type, st.occ.union(Occ.ZERO));
    return this;
  }
}
