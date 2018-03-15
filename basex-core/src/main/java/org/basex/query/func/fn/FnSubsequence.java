package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.file.*;
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
public class FnSubsequence extends StandardFunc {
  /** Return all values. */
  private static final long[] ALL = {};

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    // no range: return empty sequence
    final long[] range = range(qc);
    if(range == null) return Empty.ITER;

    // create iterator
    final Iter iter = exprs[0].iter(qc);
    if(range == ALL) return iter;

    // return empty iterator if iterator yields 0 items
    final long sz = iter.size();
    if(sz == 0) return Empty.ITER;

    // compute start, length
    final long s = range[0], l = range[1], max = l == Long.MAX_VALUE ? l : s + l;
    final long start = Math.max(1, s), end = Math.min(max, sz + 1);

    // return subsequence iterator if iterator is value-based
    final Value value = iter.value();
    final long size = Math.max(0, end - start);
    if(value != null) return value.subSequence(start - 1, size, qc).iter();

    // return optimized iterator if result size is known
    if(sz > 0) {
      if(size == 0) return Empty.ITER;
      return new Iter() {
        long c = start;

        @Override
        public Item next() throws QueryException {
          qc.checkStop();
          return c < end ? iter.get(c++ - 1) : null;
        }
        @Override
        public Item get(final long i) throws QueryException {
          return iter.get(start + i - 1);
        }
        @Override
        public long size() {
          return size;
        }
      };
    }

    // otherwise, return standard iterator
    return new Iter() {
      long c;

      @Override
      public Item next() throws QueryException {
        for(Item item; (item = qc.next(iter)) != null && ++c < max;) {
          if(c >= start) return item;
        }
        return null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final long[] range = range(qc);
    if(range == null) return Empty.SEQ;
    final Expr expr = exprs[0];
    if(range == ALL) return expr.value(qc);

    final long st = range[0], ln = range[1];
    final Iter iter = expr.iter(qc);
    final long size = iter.size();

    // return subsequence if iterator is value-based
    final Value value = iter.value();
    if(value != null) {
      final long start = Math.max(0, st - 1);
      final long length = Math.min(size - start, ln + Math.min(0, st - 1));
      return length <= 0 ? Empty.SEQ : value.subSequence(start, length, qc);
    }

    // take fast route if result size is known
    if(size >= 0) {
      final long start = Math.max(0, st - 1);
      final long length = Math.min(size - start, ln + Math.min(0, st - 1));
      if(start >= size || length <= 0) return Empty.SEQ;
      if(start == 0 && length == size) return iter.value(qc);
      final ValueBuilder vb = new ValueBuilder(qc);
      for(long i = 0; i < length; i++) vb.add(iter.get(start + i));
      return vb.value();
    }

    // otherwise, retrieve all items
    final long max = ln == Long.MAX_VALUE ? ln : st + ln;
    final ValueBuilder vb = new ValueBuilder(qc);
    Item item;
    for(int i = 1; i < max && (item = qc.next(iter)) != null; i++) {
      if(i >= st) vb.add(item);
    }
    return vb.value();
  }

  /**
   * Returns the start position and length of the requested sub sequence.
   * @param qc query context
   * @return range (start and length) or {@code null}
   * @throws QueryException query exception
   */
  private long[] range(final QueryContext qc) throws QueryException {
    final double st = toDouble(exprs[1], qc);
    if(Double.isNaN(st)) return null;

    final long start = start(st);
    final boolean min = start == Long.MIN_VALUE;
    long length = Long.MAX_VALUE;

    final int el = exprs.length;
    if(el > 2) {
      final double ln = toDouble(exprs[2], qc);
      if(Double.isNaN(ln)) return null;
      if(min && ln == Double.POSITIVE_INFINITY) return null;
      length = length(ln);
    }

    // return all values, no values, or the specified range
    return min ? length == Long.MAX_VALUE ? ALL : null :
      new long[] { start, length(start, length) };
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
   * Returns the length.
   * @param value double value
   * @return long value
   */
  public long length(final double value) {
    return StrictMath.round(value);
  }

  /**
   * Computes the count of items to be returned.
   * @param start start
   * @param length length
   * @return length
   */
  public long length(@SuppressWarnings("unused") final long start, final long length) {
    return length;
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = exprs[0];
    final SeqType st = expr.seqType();
    if(st.zero()) return expr;
    exprType.assign(st.type, st.occ.union(Occ.ZERO));

    if(exprs[1] instanceof Value && (exprs.length < 3 || exprs[2] instanceof Value)) {
      final long[] range = range(cc.qc);
      if(range != null) {
        // all values?
        if(range == ALL) return expr;
        // faster retrieval of specified lines
        return FileReadTextLines.rewrite(this, range[0], range[1], cc, info);
      }
    }
    return this;
  }
}
