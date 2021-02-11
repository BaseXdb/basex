package org.basex.query.func.bin;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BinPart extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final B64 b64 = toB64(exprs[0], qc, true);
    final Long off = toLong(exprs[1], qc);
    final Long len = exprs.length > 2 ? toLong(exprs[2], qc) : null;
    if(b64 == null) return Empty.VALUE;

    final byte[] bytes = b64.binary(info);
    final int[] bounds = bounds(off, len, bytes.length);

    final int o = bounds[0], tl = bounds[1];
    return B64.get(Arrays.copyOfRange(bytes, o, o + tl));
  }
}
