package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.List;
import org.basex.query.func.*;
import org.basex.query.func.file.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class FnSubsequence extends StandardFunc {
  /** All values. */
  private static final SeqRange ALL = new SeqRange(0, Long.MAX_VALUE);
  /** No values. */
  private static final SeqRange EMPTY = new SeqRange(0, 0);

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    // no range: return empty sequence
    final SeqRange sr = range(qc);
    if(sr == EMPTY) return Empty.ITER;

    // return iterator if all results are returned, of it iterator yields no items
    final Iter iter = exprs[0].iter(qc);
    if(sr == ALL) return iter;

    // return subsequence iterator if iterator is value-based
    final long size = sr.adjust(iter.size());
    if(sr.length == 0) return Empty.ITER;

    if(size != -1) {
      final Value value = iter.iterValue();
      if(value != null) return value.subsequence(sr.start, sr.length, qc).iter();

      if(sr.length == size) return iter;
      return new Iter() {
        long c = sr.start;

        @Override
        public Item next() throws QueryException {
          qc.checkStop();
          return c < sr.end ? iter.get(c++) : null;
        }
        @Override
        public Item get(final long i) throws QueryException {
          return iter.get(sr.start + i);
        }
        @Override
        public long size() {
          return sr.length;
        }
      };
    }

    // otherwise, return standard iterator
    return new Iter() {
      long c;

      @Override
      public Item next() throws QueryException {
        for(Item item; c < sr.end && (item = qc.next(iter)) != null;) {
          if(++c > sr.start) return item;
        }
        return null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final SeqRange sr = range(qc);
    if(sr == EMPTY) return Empty.VALUE;

    final Expr expr = exprs[0];
    if(sr == ALL) return expr.value(qc);
    final Iter iter = expr.iter(qc);

    final long size = sr.adjust(iter.size());
    if(sr.length == 0) return Empty.VALUE;

    // take fast route if result size is known
    if(size != -1) {
      // return subsequence if iterator is value-based
      final Value value = iter.iterValue();
      if(value != null) return value.subsequence(sr.start, sr.length, qc);

      if(sr.length == size) return iter.value(qc, this);

      final ValueBuilder vb = new ValueBuilder(qc);
      for(long i = sr.start; i < sr.end; i++) vb.add(iter.get(i));
      return vb.value(this);
    }

    // otherwise, retrieve all items
    final ValueBuilder vb = new ValueBuilder(qc);
    long c = 0;
    for(Item item; c < sr.end && (item = qc.next(iter)) != null; c++) {
      if(c >= sr.start) vb.add(item);
    }
    return vb.value(this);
  }

  /**
   * Returns the start position and length of the requested sub sequence.
   * @param cc compilation context
   * @return range or {@code null}
   * @throws QueryException query exception
   */
  SeqRange range(final CompileContext cc) throws QueryException {
    return exprs[1] instanceof Value && (exprs.length < 3 || exprs[2] instanceof Value) ?
      range(cc.qc) : null;
  }

  /**
   * Returns the start position and length of the requested sub sequence.
   * @param qc query context
   * @return range (start, end, length)
   * @throws QueryException query exception
   */
  private SeqRange range(final QueryContext qc) throws QueryException {
    double d = toDouble(exprs[1], qc);
    if(Double.isNaN(d)) return EMPTY;
    final long start = start(d);

    long end = Long.MAX_VALUE;
    if(exprs.length > 2) {
      d = toDouble(exprs[2], qc);
      if(Double.isNaN(d) || start == Long.MIN_VALUE && d == Double.POSITIVE_INFINITY) return EMPTY;
      end = end(start, d);
    }
    if(end == Long.MAX_VALUE && start <= 1) return ALL;
    if(start == Long.MIN_VALUE) return EMPTY;

    // return all values, no values, or the specified range
    final SeqRange sr = new SeqRange(Math.max(0, start - 1), end);
    return sr.length == 0 ? EMPTY : sr;
  }

  /**
   * Returns the start position.
   * @param value double value
   * @return long value
   */
  public long start(final double value) {
    return StrictMath.round(value);
  }

  /**
   * Computes the count of items to be returned.
   * @param first first argument
   * @param second second argument
   * @return length
   */
  public long end(final long first, final double second) {
    final long l = StrictMath.round(second);
    return l == Long.MAX_VALUE ? l : l + first - 1;
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    // ignore standard limitation for large values
    final Expr expr = exprs[0];
    final SeqType st = expr.seqType();
    if(st.zero()) return expr;

    long sz = -1;
    final SeqRange sr = range(cc);
    if(sr != null) {
      // no results
      if(sr == EMPTY) return Empty.VALUE;
      // all values?
      if(sr == ALL) return expr;
      // ignore standard limitation for large values to speed up evaluation of result
      if(expr instanceof Value) return value(cc.qc);

      // check if result size is statically known
      final long size = sr.adjust(expr.size());
      if(size != -1) {
        if(sr.length == size) return expr;
        // rewrite nested function calls
        if(sr.start == size - 1) return cc.function(_UTIL_LAST, info, expr);
        if(sr.start == 1 && sr.end == size) return cc.function(TAIL, info, expr);
        if(sr.start == 0 && sr.end == size - 1) return cc.function(_UTIL_INIT, info, expr);
        sz = sr.length;
      } else if(st.zeroOrOne()) {
        // sr.length is always larger than 0 at this point
        return sr.start == 0 ? expr : Empty.VALUE;
      }

      // rewrite nested function calls
      if(sr.length == 1) {
        return sr.start == 0 ? cc.function(HEAD, info, expr) :
          cc.function(_UTIL_ITEM, info, expr, Int.get(sr.start + 1));
      }
      if(sr.length == Long.MAX_VALUE && sr.start == 1)
        return cc.function(TAIL, info, expr);
      if(_FILE_READ_TEXT_LINES.is(expr))
        return FileReadTextLines.opt(this, sr.start, sr.length, cc);
      if(_UTIL_REPLICATE.is(expr)) {
        final Expr[] args = expr.args();
        if(args[0].size() == 1 && args[1] instanceof Int) {
          args[1] = Int.get(sr.length);
          return cc.function(_UTIL_REPLICATE, info, args);
        }
      }
      if(expr instanceof List) {
        final Expr[] args = expr.args();
        if(((Checks<Expr>) ex -> ex.seqType().one()).all(args)) {
          return List.get(cc, info, Arrays.copyOfRange(args, (int) sr.start, (int) (sr.end)));
        }
      }
    } else {
      // subsequence(expr, 1, count(expr) - 1)  ->  util:init(expr)
      if(exprs[1] == Int.ONE && exprs[2] instanceof Arith && !exprs[0].has(Flag.NDT)) {
        final Arith ar = (Arith) exprs[2];
        if(COUNT.is(ar.exprs[0]) && ar.calc == Calc.MINUS && ar.exprs[1] == Int.ONE &&
            exprs[0].equals(ar.exprs[0].arg(0))) {
          return cc.function(_UTIL_INIT, info, expr);
        }
      }
    }

    exprType.assign(st.union(Occ.ZERO), sz);
    data(expr.data());
    return this;
  }

  @Override
  public final boolean ddo() {
    return exprs[0].ddo();
  }
}
