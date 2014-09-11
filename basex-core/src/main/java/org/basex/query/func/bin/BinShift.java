package org.basex.query.func.bin;

import java.math.*;

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
public final class BinShift extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final B64 b64 = toB64(exprs[0], qc, true);
    long by = toLong(exprs[1], qc);
    if(b64 == null) return null;
    if(by == 0) return b64;

    byte[] bytes = b64.binary(info);
    final int bl = bytes.length;

    byte[] tmp = new byte[bl];
    int r = 0;
    if(by > 7) {
      tmp = new BigInteger(bytes).shiftLeft((int) by).toByteArray();
      if(tmp.length != bl) {
        bytes = tmp;
        tmp = new byte[bl];
        System.arraycopy(bytes, bytes.length - bl, tmp, 0, bl);
      }
    } else if(by > 0) {
      for(int i = bl - 1; i >= 0; i--) {
        final byte b = bytes[i];
        tmp[i] = (byte) (b << by | r);
        r = b >>> 32 - by;
      }
    } else if(by > -8) {
      by = -by;
      for(int i = 0; i < bl; i++) {
        final int b = bytes[i] & 0xFF;
        tmp[i] = (byte) (b >>> by | r);
        r = b << 32 - by;
      }
    } else {
      by = -by;
      BigInteger bi = new BigInteger(bytes);
      if(bi.signum() >= 0) {
        bi = bi.shiftRight((int) by);
      } else {
        final BigInteger o = BigInteger.ONE.shiftLeft(bl * 8 + 1);
        final BigInteger m = o.subtract(BigInteger.ONE).shiftRight((int) by + 1);
        bi = bi.subtract(o).shiftRight((int) by).and(m);
      }
      tmp = bi.toByteArray();
      final int tl = tmp.length;
      if(tl != bl) {
        bytes = tmp;
        tmp = new byte[bl];
        System.arraycopy(bytes, 0, tmp, bl - tl, tl);
      }
    }
    return new B64(tmp);
  }
}
