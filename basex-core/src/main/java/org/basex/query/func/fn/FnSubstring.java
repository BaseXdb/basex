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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnSubstring extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // normalize positions
    final byte[] str = toEmptyToken(exprs[0], qc);

    final Item is = toItem(exprs[1], qc);
    int s;
    if(is instanceof Int) {
      s = (int) is.itr(info) - 1;
    } else {
      final double ds = is.dbl(info);
      if(Double.isNaN(ds)) return Str.ZERO;
      s = subPos(ds);
    }

    final boolean end = exprs.length == 3, ascii = ascii(str);
    int l = ascii ? str.length : length(str);
    int e = l;
    if(end) {
      final Item ie = toItem(exprs[2], qc);
      e = ie instanceof Int ? (int) ie.itr(info) : subPos(ie.dbl(info) + 1);
    }
    if(s < 0) {
      e += s;
      s = 0;
    }
    e = Math.min(l, end ? s + e : Integer.MAX_VALUE);
    if(s >= e) return Str.ZERO;
    if(ascii) return Str.get(substring(str, s, e));

    int ss = s;
    int ee = e;
    int p = 0;
    for(l = 0; l < str.length; l += cl(str, l), ++p) {
      if(p == s) ss = l;
      if(p == e) ee = l;
    }
    if(p == e) ee = l;
    return Str.get(Arrays.copyOfRange(str, ss, ee));
  }

  /**
   * Returns the specified substring position.
   * @param d double value
   * @return substring position
   */
  private static int subPos(final double d) {
    final int i = (int) d;
    return d == i ? i - 1 : (int) StrictMath.floor(d - .5);
  }
}
