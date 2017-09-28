package org.basex.query.func.convert;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class ConvertBinaryToInteger extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final byte[] bytes = toBin(exprs[0], qc).binary(info);
    return new BasicIter<Int>(bytes.length) {
      @Override
      public Int get(final long i) {
        return Int.get(bytes[(int) i] & 0xFF);
      }
      @Override
      public Value value(final QueryContext q) {
        return toValue(bytes);
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return toValue(toBin(exprs[0], qc).binary(info));
  }

  /**
   * Returns a value representation of the specified bytes.
   * @param bytes bytes to be wrapped in a value
   * @return value
   */
  public static Value toValue(final byte[] bytes) {
    final int bl = bytes.length;
    if(bl == 1) return Int.get(bytes[0] & 0xFF);

    final long[] tmp = new long[bl];
    for(int b = 0; b < bl; b++) tmp[b] = bytes[b] & 0xFF;
    return IntSeq.get(tmp, AtomType.ITR);
  }
}
