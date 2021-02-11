package org.basex.query.func.hof;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class HofDropWhile extends HofTakeWhile {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final FItem pred = checkArity(exprs[1], 1, qc);

    // check if iterator is value-based
    final Value value = value(iter, pred, qc);
    if(value != null) return value.iter();

    return new Iter() {
      boolean found;

      @Override
      public Item next() throws QueryException {
        Item item = qc.next(iter);
        if(!found) {
          while(item != null && test(pred, item, qc)) item = qc.next(iter);
          found = true;
        }
        return item;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final FItem pred = checkArity(exprs[1], 1, qc);

    // check if iterator is value-based
    final Value value = value(iter, pred, qc);
    if(value != null) return value;

    final ValueBuilder vb = new ValueBuilder(qc);
    Item item;
    while((item = qc.next(iter)) != null && test(pred, item, qc));
    do vb.add(item); while((item = qc.next(iter)) != null);
    return vb.value(this);
  }

  /**
   * Returns the result value if the iterator is value-based.
   * @param iter iterator
   * @param pred predicate
   * @param qc query context
   * @return resulting value or {@code null}
   * @throws QueryException query exception
   */
  private Value value(final Iter iter, final FItem pred, final QueryContext qc)
      throws QueryException {

    final Value value = iter.iterValue();
    if(value == null) return null;

    final long size = value.size();
    long c = -1;
    while(++c < size && test(pred, value.itemAt(c), qc));
    return value.subsequence(c, size - c, qc);
  }
}
