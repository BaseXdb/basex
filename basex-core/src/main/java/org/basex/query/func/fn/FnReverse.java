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
    final Iter iter = exprs[0].iter(qc);

    // materialize value if number of results is unknown
    final long s = iter.size();
    // no result: empty iterator
    if(s == 0) return Empty.ITER;
    // single result: iterator
    if(s == 1) return iter;

    // value-based iterator
    final Value v = iter.value();
    if(v != null) return v.reverse().iter();

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
    for(Item it; (it = qc.next(iter)) != null;) vb.addFront(it);
    return vb.value().iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return exprs[0].value(qc).reverse();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr ex = exprs[0];
    return ex instanceof RangeSeq ? ((RangeSeq) ex).reverse() :
      ex instanceof SingletonSeq || ex.seqType().zeroOrOne() ? ex : adoptType(ex);
  }
}
