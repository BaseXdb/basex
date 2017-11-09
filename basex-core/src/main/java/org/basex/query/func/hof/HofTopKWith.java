package org.basex.query.func.hof;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
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
public final class HofTopKWith extends HofFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Comparator<Item> cmp = getComp(1, qc);
    final long k = Math.min(toLong(exprs[2], qc), Integer.MAX_VALUE);
    if(k < 1) return Empty.SEQ;

    final Iter iter = qc.iter(exprs[0]);
    final MinHeap<Item, Item> heap = new MinHeap<>(cmp);
    try {
      for(Item it; (it = iter.next()) != null;) {
        qc.checkStop();
        heap.insert(it, it);
        if(heap.size() > k) heap.removeMin();
      }
    } catch(final QueryRTException ex) { throw ex.getCause(); }

    final ValueBuilder vb = new ValueBuilder();
    while(!heap.isEmpty()) vb.addFront(heap.removeMin());
    return vb.value();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr ex = exprs[0];
    final SeqType st = ex.seqType();
    if(st.zero()) return ex;
    seqType = st;
    size = ex.size();
    return this;
  }
}
