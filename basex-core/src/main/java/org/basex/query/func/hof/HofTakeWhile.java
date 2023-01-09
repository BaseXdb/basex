package org.basex.query.func.hof;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Leo Woerteler
 */
public class HofTakeWhile extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter input = exprs[0].iter(qc);
    final FItem predicate = toFunction(exprs[1], 1, qc);

    // value-based iterator
    final Value value = value(input, predicate, qc);
    if(value != null) return value.iter();

    return new Iter() {
      @Override
      public Item next() throws QueryException {
        final Item item = qc.next(input);
        if(item != null && test(item, predicate, qc)) return item;
        return null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter input = exprs[0].iter(qc);
    final FItem predicate = toFunction(exprs[1], 1, qc);

    // value-based iterator
    final Value value = value(input, predicate, qc);
    if(value != null) return value;

    final ValueBuilder vb = new ValueBuilder(qc);
    for(Item item; (item = qc.next(input)) != null && test(item, predicate, qc);) {
      vb.add(item);
    }
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

    if(!input.valueIter()) return null;

    final Value value = input.value(qc, null);
    final long size = value.size();
    long c = -1;
    while(++c < size && test(value.itemAt(c), predicate, qc));
    return value.subsequence(0, c, qc);
  }

  /**
   * Tests if the specified predicate is successful.
   * @param item item
   * @param predicate predicate
   * @param qc query context
   * @return result of check
   * @throws QueryException query exception
   */
  final boolean test(final Item item, final FItem predicate, final QueryContext qc)
      throws QueryException {
    return toBoolean(predicate.invoke(qc, info, item), qc);
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    final Expr input = exprs[0];
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    exprType.assign(st.union(Occ.ZERO)).data(input);
    return this;
  }
}
