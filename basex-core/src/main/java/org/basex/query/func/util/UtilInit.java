package org.basex.query.func.util;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UtilInit extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    // retrieve and decrement iterator size
    final Iter iter = exprs[0].iter(qc);
    final long size = iter.size();

    // return empty iterator if iterator yields 0 or 1 items, or if result is an empty sequence
    if(size == 0 || size == 1) return Empty.ITER;

    // check if iterator is value-based
    final Value value = iter.iterValue();
    if(value != null) return value.subsequence(0, size - 1, qc).iter();

    // return optimized iterator if result size is known
    if(size != -1) return new Iter() {
      int n;
      @Override
      public Item next() throws QueryException {
        return ++n < size ? qc.next(iter) : null;
      }
      @Override
      public Item get(final long i) throws QueryException {
        return iter.get(i);
      }
      @Override
      public long size() {
        return size - 1;
      }
    };

    // otherwise, return standard iterator
    return new Iter() {
      Item last = iter.next();

      @Override
      public Item next() throws QueryException {
        final Item item = last;
        if(item != null) {
          last = qc.next(iter);
          if(last == null) return null;
        }
        return item;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // return empty sequence if value has 0 or 1 items
    final Value value = exprs[0].value(qc);
    final long size = value.size();
    return size < 1 ? value : value.subsequence(0, size - 1, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // ignore standard limitation for large values to speed up evaluation of result
    final Expr expr = exprs[0];
    if(expr instanceof Value) return value(cc.qc);

    final SeqType st = expr.seqType();
    if(st.zeroOrOne()) return Empty.VALUE;

    final long size = expr.size();
    if(size != -1) {
      // two results: return first item
      if(size == 2) return cc.function(HEAD, info, expr);
      // rewrite nested function calls
      if(_UTIL_INIT.is(expr))
        return cc.function(SUBSEQUENCE, info, expr.arg(0), Int.ONE, Int.get(size - 1));
    }

    if(SUBSEQUENCE.is(expr) || _UTIL_RANGE.is(expr)) {
      final SeqRange r = SeqRange.get(expr, cc);
      if(r != null) return cc.function(SUBSEQUENCE, info, expr.arg(0),
          Int.get(r.start + 1), Int.get(r.length - 1));
    }
    if(_UTIL_REPLICATE.is(expr)) {
      final Expr[] args = expr.args();
      if(args[1] instanceof Int && args[0].seqType().zeroOrOne()) {
        args[1] = Int.get(((Int) args[1]).itr() - 1);
        return cc.function(_UTIL_REPLICATE, info, args);
      }
    }

    // rewrite list
    if(expr instanceof List) {
      final Expr[] args = expr.args();
      final Expr last = args[args.length - 1];
      if(last.seqType().oneOrMore()) {
        args[args.length - 1] = cc.function(_UTIL_INIT, info, last);
        return List.get(cc, info, args);
      }
    }

    exprType.assign(st.union(Occ.ZERO), size - 1);
    data(expr.data());
    return this;
  }
}
