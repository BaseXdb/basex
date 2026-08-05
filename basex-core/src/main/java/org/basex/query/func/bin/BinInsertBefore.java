package org.basex.query.func.bin;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinInsertBefore extends BinFn {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final Bin value = toBinOrNull(arg(0), qc);
    final Long offset = toLongOrNull(arg(1), qc);
    final Bin extra = toBinOrNull(arg(2), qc);
    if(value == null) return Empty.VALUE;

    final byte[] bytes = value.binary(info);
    final int bl = bytes.length;
    final int[] bounds = bounds(offset, null, bl);

    if(extra == null) return value instanceof final B64 b64 ? b64 : B64.get(bytes);
    final byte[] xtr = extra.binary(info);
    final int xl = xtr.length;

    final byte[] tmp = new byte[bl + xl];
    final int o = bounds[0];
    Array.copy(bytes, o, tmp);
    Array.copyFromStart(xtr, xl, tmp, o);
    Array.copy(bytes, o, bl - o, tmp, o + xl);
    return B64.get(tmp);
  }
}
