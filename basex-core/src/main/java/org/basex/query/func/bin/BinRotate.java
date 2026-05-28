package org.basex.query.func.bin;

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
public final class BinRotate extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Bin value = toBinOrNull(arg(0), qc);
    final long by = toLong(arg(1), qc);
    if(value == null) return Empty.VALUE;

    final byte[] bytes = value.binary(info);
    final int bl = bytes.length;
    final long bits = (long) bl << 3;
    // normalized left rotation amount
    final int r = bits == 0 ? 0 : (int) ((by % bits + bits) % bits);
    if(r == 0) return value;

    final byte[] tmp = new byte[bl];
    for(int i = 0; i < bits; i++) {
      final int s = (int) ((i + r) % bits);
      if((bytes[s >>> 3] & 0x80 >>> (s & 7)) != 0) tmp[i >>> 3] |= 0x80 >>> (i & 7);
    }
    return B64.get(tmp);
  }
}
