package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
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
    // normalize positions
    final byte[] string = toEmptyToken(exprs[0], qc);

    final Item item = toAtomItem(exprs[1], qc);
    int start;
    if(item instanceof Int) {
      start = (int) item.itr(info) - 1;
    } else {
      final double dbl = item.dbl(info);
      if(Double.isNaN(dbl)) return Str.ZERO;
      start = subPos(dbl);
    }

    final boolean ascii = ascii(string);
    int length = ascii ? string.length : length(string);
    int end = length;
    if(exprs.length == 3) {
      final Item ie = toAtomItem(exprs[2], qc);
      end = ie instanceof Int ? (int) ie.itr(info) : subPos(ie.dbl(info) + 1);
    }
    if(start < 0) {
      end += start;
      start = 0;
    }
    end = Math.min(length, exprs.length == 3 ? start + end : Integer.MAX_VALUE);
    if(start >= end) return Str.ZERO;
    if(ascii) return Str.get(substring(string, start, end));

    // process strings with non-ascii characters
    int ss = start, ee = end, p = 0;
    final int sl = string.length;
    for(length = 0; length < sl; length += cl(string, length), ++p) {
      if(p == start) ss = length;
      if(p == end) ee = length;
    }
    if(p == end) ee = length;
    return Str.get(Arrays.copyOfRange(string, ss, ee));
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
