package org.basex.query.func.hof;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Leo Woerteler
 */
public final class HofDropWhile extends HofTakeWhile {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter input = exprs[0].iter(qc);
    final FItem predicate = toFunction(exprs[1], 1, qc);

    // check if iterator is value-based
    final Value value = value(input, predicate, qc);
    if(value != null) return value.iter();

    return new Iter() {
      boolean found;

      @Override
      public Item next() throws QueryException {
        Item item = qc.next(input);
        if(!found) {
          while(item != null && test(item, predicate, qc)) item = qc.next(input);
          found = true;
        }
        return item;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter input = exprs[0].iter(qc);
    final FItem predicate = toFunction(exprs[1], 1, qc);

    // check if iterator is value-based
    final Value value = value(input, predicate, qc);
    if(value != null) return value;

    final ValueBuilder vb = new ValueBuilder(qc);
    Item item;
    while((item = qc.next(input)) != null && test(item, predicate, qc));
    do vb.add(item); while((item = qc.next(input)) != null);
    return vb.value(this);
  }

  /**
   * Returns the result value if the iterator is value-based.
   * @param input iterator
   * @param predicate predicate
   * @param qc query context
   * @return resulting value or {@code null}
   * @throws QueryException query exception
   */
  private Value value(final Iter input, final FItem predicate, final QueryContext qc)
      throws QueryException {

    final Value value = input.iterValue();
    if(value == null) return null;

    final long size = value.size();
    long c = -1;
    while(++c < size && test(value.itemAt(c), predicate, qc));
    return value.subsequence(c, size - c, qc);
  }
}
