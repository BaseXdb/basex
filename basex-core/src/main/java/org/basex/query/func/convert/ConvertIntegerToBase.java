package org.basex.query.func.convert;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ConvertIntegerToBase extends StandardFunc {
  /** BigInteger representing 2 * ({@link Long#MAX_VALUE} + 1). */
  private static final BigInteger MAX_ULONG = BigInteger.ONE.shiftLeft(64);
  /** Digits used in base conversion. */
  private static final byte[] DIGITS = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b',
    'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
    'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
  };

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final long num = toLong(exprs[0], qc), base = toLong(exprs[1], qc);
    if(base < 2 || base > 36) throw CONVERT_BASE_X.get(info, base);

    // use fast variant for powers of two
    for(int i = 1, p = 2; i < 6; i++, p <<= 1)
      if(base == p) return toBaseFast(num, i);

    final ByteList bl = new ByteList();
    long n = num;
    if(n < 0) {
      // unsigned value doesn't fit in any native type...
      final BigInteger[] dr = BigInteger.valueOf(n).add(MAX_ULONG).divideAndRemainder(
          BigInteger.valueOf(base));
      n = dr[0].longValue();
      bl.add(DIGITS[dr[1].intValue()]);
    } else {
      bl.add(DIGITS[(int) (n % base)]);
      n /= base;
    }
    while(n != 0) {
      bl.add(DIGITS[(int) (n % base)]);
      n /= base;
    }
    return Str.get(bl.reverse().finish());
  }

  /**
   * Converts the given number to a string, using base
   * 2<sup>shift</sup>.
   * @param num number item
   * @param shift number of bits to use for one digit
   * @return string representation of the given number
   */
  private static Str toBaseFast(final long num, final int shift) {
    final byte[] bytes = new byte[(64 + shift - 1) / shift];
    final int mask = (1 << shift) - 1;
    long n = num;
    int pos = bytes.length;
    do {
      bytes[--pos] = DIGITS[(int) (n & mask)];
      n >>>= shift;
    } while(n != 0);
    return Str.get(substring(bytes, pos));
  }
}
