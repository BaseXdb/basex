package org.basex.query.func.hof;

import java.util.*;

import org.basex.query.*;
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
public final class HofTopKWith extends HofFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Comparator<Item> cmp = getComp(1, qc);
    final long k = toLong(exprs[2], qc);
    if(k < 1 || k > Integer.MAX_VALUE / 2) return Empty.SEQ;

    final Iter iter = exprs[0].iter(qc);
    final MinHeap<Item, Item> heap = new MinHeap<>((int) k, cmp);

    try {
      for(Item it; (it = iter.next()) != null;) {
        heap.insert(it, it);
        if(heap.size() > k) heap.removeMin();
      }
    } catch(final QueryRTException ex) { throw ex.getCause(); }

    final Item[] arr = new Item[heap.size()];
    for(int i = arr.length; --i >= 0;) arr[i] = heap.removeMin();
    return Seq.get(arr);
  }
}
