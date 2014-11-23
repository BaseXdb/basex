package org.basex.query.func.bin;

import static org.basex.query.QueryError.*;

import java.nio.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class BinPackInteger extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    long b = toLong(exprs[0], qc);
    final long sz = toLong(exprs[1], qc);
    final ByteOrder bo = order(2, qc);
    if(sz < 0) throw BIN_NS_X.get(info, sz);

    final byte[] tmp = new byte[(int) sz];
    final int tl = tmp.length;
    if(bo == ByteOrder.BIG_ENDIAN) {
      for(int t = tl - 1; t >= 0; t--) {
        tmp[t] = (byte) b;
        b >>= 8;
      }
    } else {
      for(int t = 0; t < tl; t++) {
        tmp[t] = (byte) b;
        b >>= 8;
      }
    }
    return new B64(tmp);
  }
}
