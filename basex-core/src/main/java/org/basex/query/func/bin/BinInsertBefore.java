package org.basex.query.func.bin;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class BinInsertBefore extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final B64 b64 = toB64(exprs[0], qc, true);
    final Long off = toLong(exprs[1], qc);
    final B64 xtr = toB64(exprs[2], qc, true);
    if(b64 == null) return null;

    final byte[] bytes = b64.binary(info);
    final int bl = bytes.length;
    final int[] bounds = bounds(off, null, bl);

    if(xtr == null) return b64;
    final byte[] extra = xtr.binary(info);
    final int xl = extra.length;

    final byte[] tmp = new byte[bl + xl];
    final int o = bounds[0];
    System.arraycopy(bytes, 0, tmp, 0, o);
    System.arraycopy(extra, 0, tmp, o, xl);
    System.arraycopy(bytes, o, tmp, o + xl, bl - o);
    return new B64(tmp);
  }
}
