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
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class HofTopKWith extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final Comparator<Item> comparator = comparator(qc);
    final long k = Math.min(toLong(arg(2), qc), Integer.MAX_VALUE);
    if(k < 1) return Empty.VALUE;

    final MinHeap<Item, Item> heap = new MinHeap<>(comparator);
    try {
      for(Item item; (item = qc.next(input)) != null;) {
        heap.insert(item, item);
        if(heap.size() > k) heap.removeMin();
      }
    } catch(final QueryRTException ex) {
      throw ex.getCause();
    }

    final ValueBuilder vb = new ValueBuilder(qc);
    while(!heap.isEmpty()) vb.addFront(heap.removeMin());
    return vb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    // even single items must be sorted, as the input might be invalid
    final Expr input = arg(0);
    return input.seqType().zero() ? input : adoptType(input);
  }

  /**
   * Gets a comparator from a less-than predicate as function item.
   * The {@link Comparator#compare(Object, Object)} method throws a
   * {@link QueryRTException} if the comparison throws a {@link QueryException}.
   * @param qc query context
   * @return comparator
   * @throws QueryException exception
   */
  private Comparator<Item> comparator(final QueryContext qc) throws QueryException {
    final FItem comparator = toFunction(arg(1), 2, qc);
    return (a, b) -> {
      try {
        return toBoolean(qc, comparator, a, b) ? -1 : 1;
      } catch(final QueryException qe) {
        throw new QueryRTException(qe);
      }
    };
  }
}
