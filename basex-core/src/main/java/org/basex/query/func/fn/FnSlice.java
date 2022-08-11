package org.basex.query.func.fn;

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
    final long size = input.size();
    final Slice s = slice(size, qc);
    if(s.empty) return Empty.VALUE;

    if(s.reverse) input = input.reverse(qc);
    final ValueBuilder vb = new ValueBuilder(qc);
    for(long i = s.start; i <= s.end; i += s.step) {
      if(i > 0 && i <= size) vb.add(input.itemAt(i - 1));
    }
    return vb.value();
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

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = exprs[0];
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    final long size = input.size();
    long sz = -1;
    final IntPredicate value = e -> e >= exprs.length || exprs[e] instanceof Value;
    if(size != -1 && value.test(1) && value.test(2) && value.test(3)) {
      final Slice s = slice(size, cc.qc);
      if(s.empty) return Empty.VALUE;
      if(s.step == 1) sz = s.end - s.start + 1;
    }

    exprType.assign(st.union(Occ.ZERO), sz);
    data(input.data());
    return this;
  }

  /**
   * Returns a slice.
   * @param size input size
   * @param qc query context
   * @return slice
   * @throws QueryException query exception
   */
  Slice slice(final long size, final QueryContext qc) throws QueryException {
    Slice s = new Slice(size, toLong(1, qc), toLong(2, qc), toLong(3, qc), false);
    if(s.step < 0) s = new Slice(size, -s.start, -s.end, -s.step, true);
    s.start = Math.max(1, s.start);
    s.end = Math.max(0, s.end);
    if(s.end < 1 || s.start > size || s.start > s.end) s.empty = true;
    return s;
  }

  /** Slice properties. */
  private static final class Slice {
    /** Reverse input. */
    private final boolean reverse;
    /** Step. */
    private final long step;
    /** Empty result. */
    private boolean empty;
    /** Start position. */
    private long start;
    /** End position. */
    private long end;

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
          ", reverse:" + reverse + ",empty:" + empty + ']';
    }
  }
}
