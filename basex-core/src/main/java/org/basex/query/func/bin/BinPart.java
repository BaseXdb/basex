package org.basex.query.func.bin;

import java.util.*;

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
public final class BinPart extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Bin value = toBinOrNull(arg(0), qc);
    final long offset = toLong(arg(1), qc);
    final Long size = toLongOrNull(arg(2), qc);
    if(value == null) return Empty.VALUE;

    final byte[] bytes = value.binary(info);
    final int[] bounds = bounds(offset, size, bytes.length);

    final int o = bounds[0], tl = bounds[1];
    return B64.get(Arrays.copyOfRange(bytes, o, o + tl));
  }
}
