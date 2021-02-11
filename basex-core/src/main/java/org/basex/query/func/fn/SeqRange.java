package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.util.*;

/**
 * Sequence range.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class SeqRange {
  /** Start position (inclusive, starting from 0). */
  public long start;
  /** End position (exclusive, starting from 0, larger than or equal to start). */
  public long end;
  /** Length (larger than or equal to 0). */
  public long length;

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
    // check if expression is fn:subsequence or util:range
    return expr instanceof FnSubsequence ? ((FnSubsequence) expr).range(cc) : null;
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
    final boolean max = e == Long.MAX_VALUE;
    start = s;
    end = max ? Long.MAX_VALUE : Math.max(s, e);
    length = max ? Long.MAX_VALUE : end - s;
  }

  @Override
  public String toString() {
    return Util.className(this) + "[start: " + start + ", end: " + end +
        ", length: " + length + ']';
  }
}