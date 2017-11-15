package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnReverse extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    // optimization: reverse sequence
    if(exprs[0] instanceof Value) return value(qc).iter();

    // materialize value if number of results is unknown
    final Iter iter = qc.iter(exprs[0]);
    final long s = iter.size();
    // no result: empty iterator
    if(s == 0) return Empty.ITER;
    // single result: iterator
    if(s == 1) return iter;

    // fast route if the size is known
    if(s > -1) return new Iter() {
      long c = s;
      @Override
      public Item next() throws QueryException {
        return --c >= 0 ? iter.get(c) : null;
      }
      @Override
      public Item get(final long i) throws QueryException {
        return iter.get(s - i - 1);
      }
      @Override
      public long size() {
        return s;
      }
    };

    // standard iterator
    final ValueBuilder vb = new ValueBuilder();
    for(Item it; (it = iter.next()) != null;) {
      qc.checkStop();
      vb.addFront(it);
    }
    return vb.value().iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return qc.value(exprs[0]).reverse();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr ex = exprs[0];
    return ex instanceof RangeSeq ? ((RangeSeq) ex).reverse() : ex.seqType().zeroOrOne() ? ex :
      adoptType(ex);
  }
}
