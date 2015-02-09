package org.basex.query.func.bin;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class BinFind extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final B64 b64 = toB64(exprs[0], qc, true);
    final Long off = toLong(exprs[1], qc);
    final B64 srch = toB64(exprs[2], qc, false);
    if(b64 == null) return null;

    final byte[] bytes = b64.binary(info);
    final int bl = bytes.length;
    final byte[] search = srch.binary(info);
    final int[] bounds = bounds(off, null, bl);
    final int pos = indexOf(bytes, search, bounds[0]);
    return pos == -1 ? null : Int.get(pos);
  }
}
