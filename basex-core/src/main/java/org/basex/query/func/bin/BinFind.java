package org.basex.query.func.bin;

import static org.basex.util.Token.*;

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
public final class BinFind extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Bin value = toBinOrNull(arg(0), qc);
    final Long offset = toLongOrNull(arg(1), qc);
    final Bin search = toBin(arg(2), qc);
    if(value == null) return Empty.VALUE;

    final byte[] bytes = value.binary(info);
    final int bl = bytes.length;
    final int pos = indexOf(bytes, search.binary(info), bounds(offset, null, bl)[0]);
    return pos == -1 ? Empty.VALUE : Itr.get(pos);
  }
}
