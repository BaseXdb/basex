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
    final long size = iter.size();
    // no result: empty iterator
    if(size == 0) return Empty.ITER;
    // single result: iterator
    if(size == 1) return iter;

    // value-based iterator
    final Value value = iter.value();
    if(value != null) return value.reverse(qc).iter();

    // fast route if the size is known
    if(size > -1) return new Iter() {
      long c = size;
      @Override
      public Item next() throws QueryException {
        qc.checkStop();
        return --c >= 0 ? iter.get(c) : null;
      }
      @Override
      public Item get(final long i) throws QueryException {
        return iter.get(size - i - 1);
      }
      @Override
      public long size() {
        return size;
      }
    };

    // standard iterator
    final ValueBuilder vb = new ValueBuilder(qc);
    for(Item item; (item = qc.next(iter)) != null;) vb.addFront(item);
    return vb.value().iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return exprs[0].value(qc).reverse(qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = exprs[0];
    return expr instanceof RangeSeq ? ((RangeSeq) expr).reverse(cc.qc) :
      expr instanceof SingletonSeq || expr.seqType().zeroOrOne() ? expr : adoptType(expr);
  }
}
