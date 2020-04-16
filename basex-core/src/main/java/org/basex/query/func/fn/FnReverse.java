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
 * @author BaseX Team 2005-20, BSD License
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
    final Value value = iter.iterValue();
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
    return vb.value(this).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return exprs[0].value(qc).reverse(qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = exprs[0];
    // zero/single items or singleton sequence: return argument
    if(expr.seqType().zeroOrOne() || expr instanceof SingletonSeq &&
        ((SingletonSeq) expr).value.size() == 1) return expr;
    // reverse sequence
    if(expr instanceof RangeSeq) return ((RangeSeq) expr).reverse(cc.qc);

    if(Function.TAIL.is(expr) && Function.REVERSE.is(args(expr)[0]))
      return cc.function(Function._UTIL_INIT, info, args(args(expr)[0]));
    if(Function._UTIL_INIT.is(expr) && Function.REVERSE.is(args(expr)[0]))
      return cc.function(Function.TAIL, info, args(args(expr)[0]));

    return adoptType(expr);
  }
}
