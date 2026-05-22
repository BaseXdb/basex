package org.basex.query.func.bin;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinSetBits extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Bin value = toBinOrNull(arg(0), qc);
    if(value == null) return Empty.VALUE;

    final boolean set = toBoolean(arg(2), qc);
    final byte[] tmp = value.binary(info).clone();
    final long bits = (long) tmp.length << 3;
    final Iter indices = arg(1).atomIter(qc, info);
    for(Item item; (item = qc.next(indices)) != null;) {
      final long index = toLong(item);
      if(index < 0 || index >= bits) throw BIN_IOOR_X_X.get(info, index, bits);
      final int i = (int) index, mask = 0x80 >>> (i & 7);
      if(set) tmp[i >>> 3] |= mask;
      else tmp[i >>> 3] &= ~mask;
    }
    return B64.get(tmp);
  }
}
