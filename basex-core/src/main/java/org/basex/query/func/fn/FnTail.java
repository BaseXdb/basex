package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class FnTail extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    // retrieve and decrement iterator size
    final Iter iter = exprs[0].iter(qc);
    final long size = iter.size() - 1;
    // return empty iterator if iterator is known to yield 0 or 1 results, or if it yields no item
    if(size == -1 || size == 0 || iter.next() == null) return Empty.ITER;

    // create new iterator, based on original iterator
    return new Iter() {
      @Override
      public Item get(final long i) throws QueryException {
        qc.checkStop();
        return iter.get(i + 1);
      }
      @Override
      public Item next() throws QueryException {
        return qc.next(iter);
      }
      @Override
      public long size() {
        return size < 0 ? -1 : size;
      }
      @Override
      public Value value(final QueryContext q) throws QueryException {
        return size < 0 ? super.value(q) : iter.value(q).subSequence(1, size, q);
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // return empty sequence if value has 0 or 1 items
    final Value value = exprs[0].value(qc);
    final long vs = value.size() - 1;
    return vs < 1 ? Empty.SEQ : value.subSequence(1, vs, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // ignore standard limitation for large values
    if(allAreValues(false)) return value(cc.qc);

    final Expr expr = exprs[0];
    final long size = expr.size() - 1;
    final SeqType st = expr.seqType();
    if(size == -1 || size == 0 || st.zeroOrOne()) return Empty.SEQ;
    exprType.assign(st.type, Occ.ZERO_MORE, size);
    return this;
  }
}
