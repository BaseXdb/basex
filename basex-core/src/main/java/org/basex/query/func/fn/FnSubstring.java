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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnSubstring extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] string = toZeroToken(exprs[0], qc);
    final boolean ascii = Token.ascii(string);
    int length = ascii ? string.length : Token.length(string);
    int start = start(qc), end = exprs.length == 3 ? length(qc) : length;

    if(length == 0 || start == Integer.MIN_VALUE) return Str.EMPTY;
    if(start < 0) {
      end += start;
      start = 0;
    }
    end = Math.min(length, exprs.length == 3 ? start + end : Integer.MAX_VALUE);
    if(start >= end) return Str.EMPTY;
    if(ascii) return Str.get(Token.substring(string, start, end));

    // process strings with non-ascii characters
    int ss = start, ee = end, p = 0;
    final int sl = string.length;
    for(length = 0; length < sl; length += Token.cl(string, length), ++p) {
      if(p == start) ss = length;
      if(p == end) ee = length;
    }
    if(p == end) ee = length;
    return Str.get(Arrays.copyOfRange(string, ss, ee));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    // empty argument: return empty string
    if(expr1 == Empty.VALUE || expr1 == Str.EMPTY) return Str.EMPTY;

    final int start = expr2 instanceof Value ? start(cc.qc) : Integer.MAX_VALUE;
    final int length = exprs.length < 3 ? Integer.MAX_VALUE :
      exprs[2] instanceof Value ? length(cc.qc) : Integer.MIN_VALUE;

    // invalid start offset or zero length: return empty string
    if(start == Integer.MIN_VALUE || length == 0) return Str.EMPTY;

    // return full string or original expression
    return start <= 0 && length == Integer.MAX_VALUE &&
      expr1.seqType().type.isStringOrUntyped() ?
      cc.function(Function.STRING, info, expr1) : this;
  }

  /**
   * Evaluates the start argument.
   * @param qc query context
   * @return start offset
   * @throws QueryException query exception
   */
  private int start(final QueryContext qc) throws QueryException {
    final Item item = toAtomItem(exprs[1], qc);
    if(item instanceof Int) return limit(item.itr(info) - 1);
    final double dbl = item.dbl(info);
    return Double.isNaN(dbl) ? Integer.MIN_VALUE : subPos(dbl);
  }

  /**
   * Evaluates the length argument.
   * @param qc query context
   * @return start offset
   * @throws QueryException query exception
   */
  private int length(final QueryContext qc) throws QueryException {
    final Item item = toAtomItem(exprs[2], qc);
    return item instanceof Int ? (int) item.itr(info) : subPos(item.dbl(info) + 1);
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
