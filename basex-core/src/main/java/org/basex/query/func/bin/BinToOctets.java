package org.basex.query.func.bin;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinToOctets extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return toIter(toBin(arg(0), qc).binary(info));
  }

  /**
   * Returns a value representation of the specified bytes.
   * @param bytes bytes to be wrapped in a value
   * @return value
   */
  public static BasicIter<Itr> toIter(final byte[] bytes) {
    return new BasicIter<>(bytes.length) {
      @Override
      public Itr get(final long i) {
        return Itr.get(bytes[(int) i] & 0xFF);
      }
      @Override
      public Value value(final QueryContext q, final Expr expr) {
        final int bl = bytes.length;
        if(bl == 1) return get(0);

        final int[] list = new int[bl];
        for(int b = 0; b < bl; b++) list[b] = bytes[b] & 0xFF;
        return IntSeq.get(list);
      }
    };
  }
}
