package org.basex.query.func.bin;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class BinFind extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final B64 binary = toB64(exprs[0], qc, true);
    final Long offset = toLong(exprs[1], qc);
    final B64 search = toB64(exprs[2], qc, false);
    if(binary == null) return Empty.VALUE;

    final byte[] bytes = binary.binary(info);
    final int bl = bytes.length;
    final int pos = indexOf(bytes, search.binary(info), bounds(offset, null, bl)[0]);
    return pos == -1 ? Empty.VALUE : Int.get(pos);
  }
}
