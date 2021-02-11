package org.basex.query.func.bin;

import java.math.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BinShift extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final B64 b64 = toB64(exprs[0], qc, true);
    final long by = toLong(exprs[1], qc);

    // special cases
    if(b64 == null) return Empty.VALUE;
    if(by == 0) return b64;

    final byte[] bytes = b64.binary(info);
    final int bl = bytes.length;

    // single byte
    if(bl == 1) {
      final int b = bytes[0];
      return B64.get((byte) (by > 0 ? (b << by) : ((b & 0xFF) >> -by)));
    }

    byte[] shifted;
    if(by / 8 >= bl || by / 8 <= -bl) {
      // too many shifts: zeroes
      shifted = new byte[bl];
    } else {
      final int shifts = (int) (by < 0 ? -by : by);
      BigInteger bi = new BigInteger(bytes);
      if(by > 0) {
        // left shift
        bi = bi.shiftLeft(shifts);
      } else {
        // right shift
        if(bi.signum() >= 0) {
          bi = bi.shiftRight(shifts);
        } else {
          final BigInteger o = BigInteger.ONE.shiftLeft((bl << 3) + 1);
          final BigInteger m = o.subtract(BigInteger.ONE).shiftRight(shifts + 1);
          bi = bi.subtract(o).shiftRight(shifts).and(m);
        }
      }
      shifted = bi.toByteArray();
    }

    // return array with identical size
    final int tl = shifted.length;
    if(tl < bl) {
      final byte[] tmp = new byte[bl];
      Array.copyFromStart(shifted, tl, tmp, bl - tl);
      shifted = tmp;
    } else if(tl > bl) {
      shifted = Arrays.copyOfRange(shifted, tl - bl, tl);
    }
    return B64.get(shifted);
  }
}
