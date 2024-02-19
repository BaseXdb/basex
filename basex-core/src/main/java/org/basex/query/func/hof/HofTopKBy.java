package org.basex.query.func.hof;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class HofTopKBy extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem key = toFunction(arg(1), 1, qc);
    final long k = Math.min(toLong(arg(2), qc), Integer.MAX_VALUE);
    if(k < 1) return Empty.VALUE;

    final MinHeap<Item, Item> heap = new MinHeap<>((item1, item2) -> {
      try {
        if(!item1.comparable(item2)) throw compareError(item1, item2, info);
        return item1.compare(item2, sc.collation, true, info);
      } catch(final QueryException qe) {
        throw new QueryRTException(qe);
      }
    });

    try {
      for(Item item; (item = input.next()) != null;) {
        final Item ky = key.invoke(qc, info, item).item(qc, info);
        heap.insert(ky.isEmpty() ? item : ky, item);
        if(heap.size() > k) heap.removeMin();
      }
    } catch(final QueryRTException ex) { throw ex.getCause(); }

    final ValueBuilder vb = new ValueBuilder(qc);
    while(!heap.isEmpty()) vb.addFront(heap.removeMin());
    return vb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr input = arg(0);
    return input.seqType().zero() ? input : adoptType(input);
  }

  @Override
  public int hofIndex() {
    return 1;
  }
}
