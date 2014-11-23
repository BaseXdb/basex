package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnSubsequence extends StandardFunc {
  /** Return all values. */
  private static final long[] ALL = {};

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final long[] range = range(qc);
    if(range == null) return Empty.ITER;
    if(range == ALL) return exprs[0].iter(qc);
    final long start = range[0], len = range[1];

    // optimization: return subsequence
    final Iter iter = qc.iter(exprs[0]);
    if(iter instanceof ValueIter) return sub((ValueIter) iter, start, len).iter();

    // fast route if the size is known
    final long max = iter.size();
    final long e = len == Long.MAX_VALUE ? len : start + len;
    if(max >= 0) return new Iter() {
      // directly access specified items
      final long m = Math.min(e, max + 1);
      long c = Math.max(1, start);
      @Override
      public Item next() throws QueryException { return c < m ? iter.get(c++ - 1) : null; }
      @Override
      public Item get(final long i) throws QueryException { return iter.get(c + i - 1); }
      @Override
      public long size() { return Math.max(0, m - c); }
    };

    // return simple iterator if number of returned values is unknown
    return new Iter() {
      long c;
      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item it = iter.next();
          if(it == null || ++c >= e) return null;
          if(c >= start) return it;
        }
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final long[] range = range(qc);
    if(range == null) return Empty.SEQ;
    if(range == ALL) return exprs[0].value(qc);
    final long start = range[0], len = range[1];

    // optimization: return subsequence
    final Iter iter = qc.iter(exprs[0]);
    if(iter instanceof ValueIter) return sub((ValueIter) iter, start, len);

    // fast route if the size is known
    final long max = iter.size();
    if(max >= 0) {
      final long s = Math.max(1, start) - 1;
      final long l = Math.min(max - s, len + Math.min(0, start - 1));
      if(s >= max || l <= 0) return Empty.SEQ;
      final ValueBuilder vb = new ValueBuilder(Math.max((int) l, 1));
      for(long i = 0; i < l; i++) vb.add(iter.get(s + i));
      return vb.value();
    }

    final long e = len == Long.MAX_VALUE ? len : start + len;
    final ValueBuilder vb = new ValueBuilder();
    Item it;
    for(int c = 1; c < e && (it = iter.next()) != null; c++) {
      if(c >= start) vb.add(it);
    }
    return vb.value();
  }

  /**
   * Returns the start position and length of the requested sub sequence.
   * @param qc query context
   * @return range
   * @throws QueryException query exception
   */
  private long[] range(final QueryContext qc) throws QueryException {
    final double ds = toDouble(exprs[1], qc);
    if(Double.isNaN(ds)) return null;

    final long start = StrictMath.round(ds);
    final boolean min = start == Long.MIN_VALUE;
    long len = Long.MAX_VALUE;

    final int el = exprs.length;
    if(el > 2) {
      final double dl = toDouble(exprs[2], qc);
      if(Double.isNaN(dl)) return null;
      if(min && dl == Double.POSITIVE_INFINITY) return null;
      len = StrictMath.round(dl);
    }
    if(min) return len == Long.MAX_VALUE ? ALL : null;

    // end flag: compute length
    if(el > 3 && toBoolean(exprs[3], qc)) len = len - start + 1;
    return min ? len == Long.MAX_VALUE ? ALL : null : new long[] { start, len };
  }


  /**
   * Returns a subsequence.
   * @param iter iterator
   * @param start start position
   * @param len length
   * @return sub sequence
   */
  private static Value sub(final ValueIter iter, final long start, final long len) {
    final Value val = iter.value();
    final long s = Math.max(1, start) - 1;
    final long l = Math.min(val.size() - s, len + Math.min(0, start - 1));
    return SubSeq.get(val, s, l);
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    final SeqType st = exprs[0].seqType();
    seqType = SeqType.get(st.type, st.zeroOrOne() ? Occ.ZERO_ONE : Occ.ZERO_MORE);
    return this;
  }
}
