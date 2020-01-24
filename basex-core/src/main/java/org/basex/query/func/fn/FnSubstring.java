package org.basex.query.func.fn;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class FnSubstring extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] string = toEmptyToken(exprs[0], qc);
    final boolean ascii = Token.ascii(string);
    int length = ascii ? string.length : Token.length(string);

    // compute start and end offset
    int start = start(qc);
    if(start == Integer.MIN_VALUE) return Str.ZERO;
    int end = exprs.length == 3 ? length(qc) : length;
    if(start < 0) {
      end += start;
      start = 0;
    }
    end = Math.min(length, exprs.length == 3 ? start + end : Integer.MAX_VALUE);
    if(start >= end) return Str.ZERO;
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
    final int start = exprs[1] instanceof Value ? start(cc.qc) : Integer.MAX_VALUE;
    final int length = exprs.length < 3 ? Integer.MAX_VALUE :
      exprs[2] instanceof Value ? length(cc.qc) : Integer.MIN_VALUE;

    // invalid start offset or zero length: return empty string
    if(start == Integer.MIN_VALUE || length == 0) return Str.ZERO;

    // return full string or original expression
    return start <= 0 && length == Integer.MAX_VALUE &&
      exprs[0].seqType().type.isStringOrUntyped() ?
      cc.function(Function.STRING, info, exprs[0]) : this;
  }

  /**
   * Evaluates the start argument.
   * @param qc query context
   * @return start offset
   * @throws QueryException query exception
   */
  private int start(final QueryContext qc) throws QueryException {
    final Item item = toAtomItem(exprs[1], qc);
    if(item instanceof Int) return (int) item.itr(info) - 1;
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
    final Item ie = toAtomItem(exprs[2], qc);
    return ie instanceof Int ? (int) ie.itr(info) : subPos(ie.dbl(info) + 1);
  }

  /**
   * Returns the specified substring position.
   * @param d double value
   * @return substring position
   */
  private static int subPos(final double d) {
    final int i = (int) d;
    return d == i ? i - 1 : (int) StrictMath.floor(d - 0.5);
  }
}
