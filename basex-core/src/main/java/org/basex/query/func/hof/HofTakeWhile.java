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
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public class HofTakeWhile extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final FItem pred = checkArity(exprs[1], 1, qc);

    // check if iterator is value-based
    final Value value = value(iter, pred, qc);
    if(value != null) return value.iter();

    return new Iter() {
      @Override
      public Item next() throws QueryException {
        final Item item = qc.next(iter);
        if(item != null && test(pred, item, qc)) return item;
        return null;
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
    for(Item item; (item = qc.next(iter)) != null && test(pred, item, qc);) vb.add(item);
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
    return value.subsequence(0, c, qc);
  }

  /**
   * Tests if the specified predicate is successful.
   * @param pred predicate
   * @param item item
   * @param qc query context
   * @return result of check
   * @throws QueryException query exception
   */
  final boolean test(final FItem pred, final Item item, final QueryContext qc)
      throws QueryException {
    return toBoolean(pred.invoke(qc, info, item), qc);
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    final Expr expr = exprs[0];
    final SeqType st = expr.seqType();
    if(st.zero()) return expr;

    exprType.assign(st.union(Occ.ZERO));
    data(expr.data());
    return this;
  }
}
