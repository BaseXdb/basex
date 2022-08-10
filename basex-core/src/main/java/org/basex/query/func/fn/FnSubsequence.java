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
 * @author BaseX Team 2005-22, BSD License
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
    final Iter input = exprs[0].iter(qc);
    if(sr == ALL) return input;

    // return subsequence iterator if iterator is value-based
    final long size = sr.adjust(input.size());
    if(sr.length == 0) return Empty.ITER;

    if(size != -1) {
      final Value value = input.iterValue();
      if(value != null) return value.subsequence(sr.start, sr.length, qc).iter();
      if(sr.length == size) return input;

      return new Iter() {
        long c = sr.start;

        @Override
        public Item next() throws QueryException {
          qc.checkStop();
          return c < sr.end ? input.get(c++) : null;
        }
        @Override
        public Item get(final long i) throws QueryException {
          return input.get(sr.start + i);
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
        for(Item item; c < sr.end && (item = qc.next(input)) != null;) {
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

    final Expr input = exprs[0];
    if(sr == ALL) return input.value(qc);
    final Iter iter = input.iter(qc);

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
   * Returns the start position and length of the requested subsequence.
   * @param cc compilation context
   * @return range or {@code null}
   * @throws QueryException query exception
   */
  SeqRange range(final CompileContext cc) throws QueryException {
    return exprs[1] instanceof Value && (exprs.length < 3 || exprs[2] instanceof Value) ?
      range(cc.qc) : null;
  }

  /**
   * Returns the start position and length of the requested subsequence.
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
    final Expr input = exprs[0];
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    long sz = -1;
    final SeqRange sr = range(cc);
    if(sr != null) {
      // no results
      if(sr == EMPTY) return Empty.VALUE;
      // all values?
      if(sr == ALL) return input;
      // ignore standard limitation for large values to speed up evaluation of result
      if(input instanceof Value) return value(cc.qc);

      // check if result size is statically known
      final long size = sr.adjust(input.size());
      if(size != -1) {
        if(sr.length == size) return input;
        // rewrite nested function calls
        if(sr.start == size - 1) return cc.function(_UTIL_LAST, info, input);
        if(sr.start == 1 && sr.end == size) return cc.function(TAIL, info, input);
        if(sr.start == 0 && sr.end == size - 1) return cc.function(_UTIL_INIT, info, input);
        sz = sr.length;
      } else if(st.zeroOrOne()) {
        // sr.length is always larger than 0 at this point
        return sr.start == 0 ? input : Empty.VALUE;
      }

      // rewrite nested function calls
      if(sr.length == 1) {
        return sr.start == 0 ? cc.function(HEAD, info, input) :
          cc.function(_UTIL_ITEM, info, input, Int.get(sr.start + 1));
      }
      if(sr.length == Long.MAX_VALUE && sr.start == 1)
        return cc.function(TAIL, info, input);
      if(_FILE_READ_TEXT_LINES.is(input))
        return FileReadTextLines.opt(this, sr.start, sr.length, cc);
      if(REPLICATE.is(input)) {
        final Expr[] args = input.args();
        if(args[0].size() == 1 && args[1] instanceof Int) {
          args[1] = Int.get(sr.length);
          return cc.function(REPLICATE, info, args);
        }
      }
      if(input instanceof List) {
        final Expr[] args = input.args();
        if(((Checks<Expr>) ex -> ex.seqType().one()).all(args)) {
          return List.get(cc, info, Arrays.copyOfRange(args, (int) sr.start, (int) sr.end));
        }
      }
    } else if(exprs[1] == Int.ONE && exprs[2] instanceof Arith && !input.has(Flag.NDT)) {
      // subsequence(expr, 1, count(expr) - 1)  ->  util:init(expr)
      final Arith arith = (Arith) exprs[2];
      if(COUNT.is(arith.exprs[0]) && arith.calc == Calc.MINUS && arith.exprs[1] == Int.ONE &&
          input.equals(arith.exprs[0].arg(0))) {
        return cc.function(_UTIL_INIT, info, input);
      }
    }

    final Expr embedded = embed(cc, false);
    if(embedded != null) return embedded;

    exprType.assign(st.union(Occ.ZERO), sz);
    data(input.data());
    return this;
  }

  @Override
  public final boolean ddo() {
    return exprs[0].ddo();
  }
}
