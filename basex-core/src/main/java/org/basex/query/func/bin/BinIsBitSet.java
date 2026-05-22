package org.basex.query.func.bin;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinIsBitSet extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Bin value = toBinOrNull(arg(0), qc);
    final long index = toLong(arg(1), qc);
    if(value == null) return Empty.VALUE;

    final byte[] bytes = value.binary(info);
    final long bits = (long) bytes.length << 3;
    if(index < 0 || index >= bits) throw BIN_IOOR_X_X.get(info, index, bits);

    final int i = (int) index;
    return Bln.get((bytes[i >>> 3] & 0x80 >>> (i & 7)) != 0);
  }
}
