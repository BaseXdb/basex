package org.basex.query.func.fn;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnSubstring extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toZeroToken(arg(0), qc);
    final boolean ascii = Token.ascii(value);
    int length = ascii ? value.length : Token.length(value);
    int start = start(qc), end = length(length, qc);

    if(length == 0 || start == Integer.MIN_VALUE) return Str.EMPTY;
    if(start < 0) {
      end += start;
      start = 0;
    }
    end = Math.min(length, defined(2) ? start + end : Integer.MAX_VALUE);
    if(start >= end) return Str.EMPTY;
    if(ascii) return Str.get(Token.substring(value, start, end));

    // process strings with non-ascii characters
    int ss = start, ee = end, p = 0;
    final int sl = value.length;
    for(length = 0; length < sl; length += Token.cl(value, length), ++p) {
      if(p == start) ss = length;
      if(p == end) ee = length;
    }
    if(p == end) ee = length;
    return Str.get(Arrays.copyOfRange(value, ss, ee));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // empty argument: return empty string
    final Expr value = arg(0);
    if(value == Empty.VALUE || value == Str.EMPTY) return Str.EMPTY;

    final int start = arg(1) instanceof Value ? start(cc.qc) : Integer.MAX_VALUE;
    final int length = !defined(2) || arg(2) instanceof Value ?
      length(Integer.MAX_VALUE, cc.qc) : Integer.MIN_VALUE;

    // invalid start offset or zero length: return empty string
    if(start == Integer.MIN_VALUE || length == 0) return Str.EMPTY;

    // return full string or original expression
    return start <= 0 && length == Integer.MAX_VALUE &&
      value.seqType().type.isStringOrUntyped() ?
      cc.function(Function.STRING, info, value) : this;
  }

  /**
   * Evaluates the start argument.
   * @param qc query context
   * @return start offset
   * @throws QueryException query exception
   */
  private int start(final QueryContext qc) throws QueryException {
    final Item start = toAtomItem(arg(1), qc);
    if(start instanceof Int) return limit(start.itr(info) - 1);
    final double dbl = start.dbl(info);
    return Double.isNaN(dbl) ? Integer.MIN_VALUE : subPos(dbl);
  }

  /**
   * Evaluates the length argument.
   * @param qc query context
   * @param def default length
   * @return start offset
   * @throws QueryException query exception
   */
  private int length(final int def, final QueryContext qc) throws QueryException {
    final Item length = arg(2).atomItem(qc, info);
    return length.isEmpty() ? def : length instanceof Int ? (int) length.itr(info) :
      subPos(length.dbl(info) + 1);
  }

  /**
   * Returns the specified substring position.
   * @param d double value
   * @return substring position
   */
  private static int subPos(final double d) {
    final int i = (int) d;
    return limit(d == i ? i - 1 : (long) StrictMath.floor(d - 0.5));
  }

  /**
   * Converts long to int, and ensures that the value does not exceed the integer limits.
   * @param l long value
   * @return integer
   */
  private static int limit(final long l) {
    return (int) Math.min(Math.max(Integer.MIN_VALUE + 1, l), Integer.MAX_VALUE - 1);
  }
}
