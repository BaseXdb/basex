package org.basex.query.func.bin;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class BinPart extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final B64 binary = toB64(exprs[0], qc, true);
    final Long offset = toLong(exprs[1], qc);
    final Long size = exprs.length > 2 ? toLong(exprs[2], qc) : null;
    if(binary == null) return Empty.VALUE;

    final byte[] bytes = binary.binary(info);
    final int[] bounds = bounds(offset, size, bytes.length);

    final int o = bounds[0], tl = bounds[1];
    return B64.get(Arrays.copyOfRange(bytes, o, o + tl));
  }
}
