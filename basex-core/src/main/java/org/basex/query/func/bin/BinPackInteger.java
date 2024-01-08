package org.basex.query.func.bin;

import static org.basex.query.QueryError.*;

import java.nio.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class BinPackInteger extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    long integer = toLong(arg(0), qc);
    final long size = toLong(arg(1), qc);
    final ByteOrder order = order(arg(2), qc);
    if(size < 0) throw BIN_NS_X.get(info, size);

    final byte[] tmp = new byte[(int) size];
    final int tl = tmp.length;
    if(order == ByteOrder.BIG_ENDIAN) {
      for(int t = tl - 1; t >= 0; t--) {
        tmp[t] = (byte) integer;
        integer >>= 8;
      }
    } else {
      for(int t = 0; t < tl; t++) {
        tmp[t] = (byte) integer;
        integer >>= 8;
      }
    }
    return B64.get(tmp);
  }
}
