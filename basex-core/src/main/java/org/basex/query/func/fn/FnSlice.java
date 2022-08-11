package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
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
public class FnSlice extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    Value input = exprs[0].value(qc);

    final Slice slice = slice(input.size(), qc);
    if(slice.length == 0) return Empty.VALUE;
    if(slice.reverse) input = input.reverse(qc);
    if(slice.step == 1) return input.subsequence(slice.start - 1, slice.length, qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    for(long i = slice.start; i <= slice.end; i += slice.step) vb.add(input.itemAt(i - 1));
    return vb.value();
  }

  @Override
  public Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = exprs[0];
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    // rewrite to util:range to trigger further rewritings
    final long size = input.size();
    final IntPredicate value = e -> e >= exprs.length || exprs[e] instanceof Value;
    if(size != -1 && value.test(1) && value.test(2) && value.test(3)) {
      final Slice slice = slice(size, cc.qc);
      if(slice.step == 1) {
        final Expr arg = slice.reverse ? cc.function(REVERSE, info, input) : input;
        return cc.function(_UTIL_RANGE, info, arg, Int.get(slice.start), Int.get(slice.end));
      }
    }

    exprType.assign(st.union(Occ.ZERO));
    data(input.data());
    return this;
  }

  /**
   * Returns slice properties.
   * @param size input size
   * @param qc query context
   * @return slice properties
   * @throws QueryException query exception
   */
  protected final Slice slice(final long size, final QueryContext qc) throws QueryException {
    Slice s = new Slice(size, toLong(1, qc), toLong(2, qc), toLong(3, qc), false);
    if(s.step < 0) s = new Slice(size, -s.start, -s.end, -s.step, true);
    s.start = Math.max(1, s.start);
    s.end = Math.min(size, s.end);
    s.length = s.end < 1 || s.start > size || s.start > s.end ? 0 : s.end - s.start + 1;
    return s;
  }

  /**
   * Returns a normalized specified integer argument.
   * @param i index of argument
   * @param qc query context
   * @return integer
   * @throws QueryException query exception
   */
  private long toLong(final int i, final QueryContext qc) throws QueryException {
    if(i < exprs.length) {
      final Item item = exprs[i].atomItem(qc, info);
      if(item != Empty.VALUE) return toLong(item);
    }
    return 0;
  }

  /** Slice properties. */
  public static final class Slice {
    /** Reverse input. */
    public final boolean reverse;
    /** Step. */
    public final long step;
    /** Result length. */
    public long length;
    /** Start position. */
    public long start;
    /** End position. */
    public long end;

    /**
     * Constructor.
     * @param size input size
     * @param start start position
     * @param end end position
     * @param step step
     * @param reverse reverse input
     */
    private Slice(final long size, final long start, final long end, final long step,
        final boolean reverse) {
      this.start = start > 0 ? start : start < 0              ? size + start + 1 : 1;
      this.end   = end   > 0 ? end   : end   < 0              ? size + end   + 1 : size;
      this.step  = step != 0 ? step  : this.start <= this.end ? 1                : -1;
      this.reverse = reverse;
    }

    @Override
    public String toString() {
      return Util.className(this) + "[start:" + start + ", end:" + end + ", step:" + step +
          ", reverse:" + reverse + ",length:" + length + ']';
    }
  }
}
