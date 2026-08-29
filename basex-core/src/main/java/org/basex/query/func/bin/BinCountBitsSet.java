package org.basex.query.func.bin;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinCountBitsSet extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Bin value = toBinOrNull(arg(0), qc);
    if(value == null) return Empty.VALUE;

    final byte[] bytes = value.binary(info);
    long c = 0;
    for(final byte b : bytes) c += Integer.bitCount(b & 0xFF);
    return Itr.get(c);
  }
}
