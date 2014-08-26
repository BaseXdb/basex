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
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final double ds = toDouble(exprs[1], qc);
    if(Double.isNaN(ds)) return Empty.ITER;
    final long s = StrictMath.round(ds);
    final boolean si = s == Long.MIN_VALUE;

    long l = Long.MAX_VALUE;
    if(exprs.length > 2) {
      final double dl = toDouble(exprs[2], qc);
      if(Double.isNaN(dl)) return Empty.ITER;
      if(si && dl == Double.POSITIVE_INFINITY) return Empty.ITER;
      l = StrictMath.round(dl);
    }
    final boolean linf = l == Long.MAX_VALUE;
    if(si) return linf ? exprs[0].iter(qc) : Empty.ITER;

    // optimization: return subsequence
    final Iter iter = qc.iter(exprs[0]);
    if(iter instanceof ValueIter) {
      final Value val = iter.value();
      final long rs = val.size();
      final long from = Math.max(1, s) - 1;
      final long len = Math.min(rs - from, l + Math.min(0, s - 1));
      return SubSeq.get(val, from, len).iter();
    }
    final long max = iter.size();
    final long e = linf ? l : s + l;

    // fast route if the size is known
    if(max >= 0) return new Iter() {
      // directly access specified items
      final long m = Math.min(e, max + 1);
      long c = Math.max(1, s);
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
          final Item i = iter.next();
          if(i == null || ++c >= e) return null;
          if(c >= s) return i;
        }
      }
    };
  }


  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final double dstart = toDouble(exprs[1], qc);
    if(Double.isNaN(dstart)) return Empty.SEQ;
    final long start = StrictMath.round(dstart);
    final boolean sinf = start == Long.MIN_VALUE;

    long l = Long.MAX_VALUE;
    if(exprs.length > 2) {
      final double dlength = toDouble(exprs[2], qc);
      if(Double.isNaN(dlength)) return Empty.SEQ;
      if(sinf && dlength == Double.POSITIVE_INFINITY) return Empty.SEQ;
      l = StrictMath.round(dlength);
    }
    final boolean linf = l == Long.MAX_VALUE;
    if(sinf) return linf ? exprs[0].value(qc) : Empty.SEQ;

    // optimization: return subsequence
    final Iter iter = qc.iter(exprs[0]);
    if(iter instanceof ValueIter) {
      final Value val = iter.value();
      final long rs = val.size();
      final long from = Math.max(1, start) - 1;
      final long len = Math.min(rs - from, l + Math.min(0, start - 1));
      return SubSeq.get(val, from, len);
    }

    // fast route if the size is known
    final long max = iter.size();
    if(max >= 0) {
      final long from = Math.max(1, start) - 1;
      final long len = Math.min(max - from, l + Math.min(0, start - 1));
      if(from >= max || len <= 0) return Empty.SEQ;
      final ValueBuilder vb = new ValueBuilder(Math.max((int) len, 1));
      for(long i = 0; i < len; i++) vb.add(iter.get(from + i));
      return vb.value();
    }

    final long e = linf ? l : start + l;
    final ValueBuilder build = new ValueBuilder();
    Item i;
    for(int c = 1; c < e && (i = iter.next()) != null; c++) {
      if(c >= start) build.add(i);
    }
    return build.value();
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    final SeqType st = exprs[0].seqType();
    seqType = SeqType.get(st.type, st.zeroOrOne() ? Occ.ZERO_ONE : Occ.ZERO_MORE);
    return this;
  }
}
