package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Sequence range.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SeqRange {
  /** Start position (inclusive, starting from 0). */
  long start;
  /** End position (exclusive, starting from 0, larger than or equal to start). */
  long end;
  /** Length (larger than or equal to 0). */
  long length;

  /**
   * Constructor.
   * @param start start value
   * @param end end value
   */
  SeqRange(final long start, final long end) {
    assign(start, end);
  }

  /**
   * Returns an instance for the specified range expression.
   * @param expr sequence expression
   * @param cc compilation context
   * @return range or {@code null}
   * @throws QueryException query exception
   */
  public static SeqRange get(final Expr expr, final CompileContext cc) throws QueryException {
    // check if expression is fn:subsequence, util:range, fn:tail, fn:trunk or fn:items-at
    if(expr instanceof final FnSubsequence sub) return sub.range(cc);
    if(TAIL.is(expr)) return new SeqRange(1, Long.MAX_VALUE);
    final long size = expr.size();
    if(TRUNK.is(expr) && size != -1) return new SeqRange(0, size);
    if(ITEMS_AT.is(expr) && expr.arg(1) instanceof final Itr itr && itr.itr() > 0) {
      return new SeqRange(itr.itr() - 1, itr.itr());
    }
    return null;
  }

  /**
   * Returns a range that is located within this range.
   * @param sub range, relative to this range
   * @return new range
   */
  SeqRange sub(final SeqRange sub) {
    if(sub.start >= length) return new SeqRange(start, start);
    final long s = start + sub.start;
    return new SeqRange(s, s + Math.min(sub.length, length - sub.start));
  }

  /**
   * Returns the input sizes for which the size of this range is within the specified bounds.
   * @param min minimum size of the range
   * @param max maximum size of the range
   * @return minimum and maximum input size, or {@code null} if no input size qualifies
   */
  public long[] inputSize(final long min, final long max) {
    if(min > length || max < 0) return null;
    return new long[] { min <= 0 ? 0 : start + min, max >= length ? Long.MAX_VALUE : start + max };
  }

  /**
   * Returns a function call that returns this range of the specified input.
   * @param input input expression
   * @param info input info (can be {@code null})
   * @param cc compilation context
   * @return function call
   * @throws QueryException query exception
   */
  Expr function(final Expr input, final InputInfo info, final CompileContext cc)
      throws QueryException {
    final Expr pos = Itr.get(start + 1);
    if(end == Long.MAX_VALUE) {
      return start == 1 ? cc.function(TAIL, info, input) :
        cc.function(SUBSEQUENCE, info, input, pos);
    }
    if(length == 1) {
      return start == 0 ? cc.function(HEAD, info, input) :
        cc.function(ITEMS_AT, info, input, pos);
    }
    return cc.function(SUBSEQUENCE, info, input, pos, Itr.get(length));
  }

  /**
   * Adjusts the range values by applying the result size.
   * @param size result size (ignored if {@code -1})
   * @return passed on result size
   */
  long adjust(final long size) {
    if(size != -1 && size < end) assign(Math.min(start, size), size);
    return size;
  }

  /**
   * Assigns a start and end position.
   * @param s start position
   * @param e end position
   */
  private void assign(final long s, final long e) {
    start = s;
    end = Math.max(s, e);
    length = end - s;
  }

  @Override
  public String toString() {
    return Util.className(this) + "[start: " + start + ", end: " + end +
        ", length: " + length + ']';
  }
}
