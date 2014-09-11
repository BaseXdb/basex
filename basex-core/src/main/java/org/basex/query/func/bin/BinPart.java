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
public final class BinPart extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final B64 b64 = toB64(exprs[0], qc, true);
    final Long off = toLong(exprs[1], qc);
    final Long len = exprs.length > 2 ? toLong(exprs[2], qc) : null;
    if(b64 == null) return null;

    final byte[] bytes = b64.binary(info);
    final int bl = bytes.length;
    final int[] bounds = bounds(off, len, bl);

    final byte[] tmp = new byte[bounds[1]];
    System.arraycopy(bytes, bounds[0], tmp, 0, bounds[1]);
    return new B64(tmp);
  }
}
