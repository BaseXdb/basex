package org.basex.query.func.hof;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class HofTopKBy extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final FItem getKey = checkArity(exprs[1], 1, qc);
    final long k = Math.min(toLong(exprs[2], qc), Integer.MAX_VALUE);
    if(k < 1) return Empty.SEQ;

    final Iter iter = qc.iter(exprs[0]);
    final MinHeap<Item, Item> heap = new MinHeap<>((it1, it2) -> {
      try {
        return OpV.LT.eval(it1, it2, sc.collation, sc, info) ? -1 : 1;
      } catch(final QueryException qe) {
        throw new QueryRTException(qe);
      }
    });

    try {
      for(Item it; (it = iter.next()) != null;) {
        qc.checkStop();
        heap.insert(checkNoEmpty(getKey.invokeItem(qc, info, it)), it);
        if(heap.size() > k) heap.removeMin();
      }
    } catch(final QueryRTException ex) { throw ex.getCause(); }

    final ValueBuilder vb = new ValueBuilder();
    while(!heap.isEmpty()) vb.addFront(heap.removeMin());
    return vb.value();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr e = exprs[0];
    final SeqType st = e.seqType();
    if(st.zero()) return e;
    seqType = st;
    size = e.size();
    return this;
  }
}
