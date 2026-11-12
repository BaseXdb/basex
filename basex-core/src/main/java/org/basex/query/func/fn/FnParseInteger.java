package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnParseInteger extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] value = toTokenOrNull(arg(0), qc);
    final Expr radix = arg(1);
    final Item rad = atomItem(radix, qc);
    if(value == null) return Empty.VALUE;

    final long rdx = rad.isEmpty() ? 10 : toLong(rad, radix);
    if(rdx < 2 || rdx > 36) throw INTRADIX_X.get(info, rdx);

    final byte[] digits = Token.digits(value);
    final int dl = digits.length;
    final byte first = dl > 0 ? digits[0] : 0;
    final boolean neg = first == '-';
    int d = neg || first == '+' ? 1 : 0;
    if(d == dl) throw INTINVALID_X_X.get(info, rdx, value);

    // accumulate negative value: the range of negative integers is larger
    long res = 0;
    boolean range = true;
    for(; d < dl; d++) {
      final byte b = digits[d];
      final int num = b <= '9' ? b - 0x30 : (b & 0xDF) - 0x37;
      if(!(b >= '0' && b <= '9' || b >= 'a' && b <= 'z' || b >= 'A' && b <= 'Z') || num >= rdx)
        throw INTINVALID_X_X.get(info, rdx, value);

      if(range) {
        try {
          res = Math.subtractExact(Math.multiplyExact(res, rdx), num);
        } catch(final ArithmeticException ex) {
          range = false;
        }
      }
    }
    if(!range || !neg && res == Long.MIN_VALUE) throw INTRANGE_X.get(info, value);
    return Itr.get(neg ? res : -res);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst();
  }
}
