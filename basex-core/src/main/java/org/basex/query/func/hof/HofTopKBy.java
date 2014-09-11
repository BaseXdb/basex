package org.basex.query.func.hof;

import java.util.*;

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
 * @author BaseX Team 2005-14, BSD License
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
    final long k = toLong(exprs[2], qc);
    if(k < 1 || k > Integer.MAX_VALUE / 2) return Empty.SEQ;

    final Iter iter = exprs[0].iter(qc);
    final MinHeap<Item, Item> heap = new MinHeap<>((int) k,
        new Comparator<Item>() {
      @Override
      public int compare(final Item it1, final Item it2) {
        try {
          return CmpV.OpV.LT.eval(it1, it2, sc.collation, info) ? -1 : 1;
        } catch(final QueryException qe) {
          throw new QueryRTException(qe);
        }
      }
    });

    try {
      for(Item it; (it = iter.next()) != null;) {
        heap.insert(checkNoEmpty(getKey.invokeItem(qc, info, it)), it);
        if(heap.size() > k) heap.removeMin();
      }
    } catch(final QueryRTException ex) { throw ex.getCause(); }

    final Item[] arr = new Item[heap.size()];
    for(int i = arr.length; --i >= 0;) arr[i] = heap.removeMin();
    return Seq.get(arr, arr.length);
  }
}
