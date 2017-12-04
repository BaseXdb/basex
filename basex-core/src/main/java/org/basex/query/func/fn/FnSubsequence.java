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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public class FnSubsequence extends StandardFunc {
  /** Return all values. */
  private static final long[] ALL = {};

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final long[] range = range(qc);
    // no range: return empty sequence
    if(range == null) return Empty.ITER;

    // return all values
    final Iter iter = exprs[0].iter(qc);
    if(range == ALL) return iter;

    // compute start, length
    final long start = range[0], len = range[1], is = iter.size();
    final long e = len == Long.MAX_VALUE ? len : start + len;
    final long s = Math.max(1, start), m = Math.min(e, is + 1), l = Math.max(0, m - s);

    // fast route if the size is known
    if(is >= 0) return new Iter() {
      // directly access specified items
      long c = s;
      @Override
      public Item next() throws QueryException {
        return c < m ? iter.get(c++ - 1) : null;
      }
      @Override
      public Item get(final long i) throws QueryException {
        return iter.get(s + i - 1);
      }
      @Override
      public long size() {
        return l;
      }
    };

    // return simple iterator if number of returned values is unknown
    return new Iter() {
      long c;
      @Override
      public Item next() throws QueryException {
        for(Item it; (it = qc.next(iter)) != null && ++c < e;) {
          if(c >= start) return it;
        }
        return null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final long[] range = range(qc);
    if(range == null) return Empty.SEQ;
    final Expr ex = exprs[0];
    if(range == ALL) return ex.value(qc);
    final long start = range[0], len = range[1];

    // return subsequence if value access is cheap
    final Iter iter = ex.iter(qc);
    final long is = iter.size();
    final Value v = iter.value();
    if(v != null) {
      final long s = Math.max(0, start - 1), l = Math.min(is - s, len + Math.min(0, start - 1));
      return l <= 0 ? Empty.SEQ : v.subSequence(s, l);
    }

    // take fast route if the size is known
    if(is >= 0) {
      final long s = Math.max(0, start - 1), l = Math.min(is - s, len + Math.min(0, start - 1));
      if(s >= is || l <= 0) return Empty.SEQ;
      if(s == 0 && l == is) return iter.value(qc);
      final ValueBuilder vb = new ValueBuilder();
      for(long i = 0; i < l; i++) {
        qc.checkStop();
        vb.add(iter.get(s + i));
      }
      return vb.value();
    }

    final long e = len == Long.MAX_VALUE ? len : start + len;
    final ValueBuilder vb = new ValueBuilder();
    Item it;
    for(int i = 1; i < e && (it = qc.next(iter)) != null; i++) {
      if(i >= start) vb.add(it);
    }
    return vb.value();
  }

  /**
   * Returns the start position and length of the requested sub sequence.
   * @param qc query context
   * @return range or {@code null}
   * @throws QueryException query exception
   */
  private long[] range(final QueryContext qc) throws QueryException {
    final double ds = toDouble(exprs[1], qc);
    if(Double.isNaN(ds)) return null;

    final long start = start(ds);
    final boolean min = start == Long.MIN_VALUE;
    long len = Long.MAX_VALUE;

    final int el = exprs.length;
    if(el > 2) {
      final double dl = toDouble(exprs[2], qc);
      if(Double.isNaN(dl)) return null;
      if(min && dl == Double.POSITIVE_INFINITY) return null;
      len = length(dl);
    }

    // return all values, no values, or the specified range
    return min ? len == Long.MAX_VALUE ? ALL : null : new long[] { start, length(start, len) };
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
  protected Expr opt(final CompileContext cc) {
    final Expr ex = exprs[0];
    final SeqType st = ex.seqType();
    if(st.zero()) return ex;
    exprType.assign(st.type, st.occ.union(Occ.ZERO));
    return this;
  }
}
