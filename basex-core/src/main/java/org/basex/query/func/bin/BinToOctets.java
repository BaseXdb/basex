package org.basex.query.func.bin;

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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class BinToOctets extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final B64 b64 = toB64(exprs[0], qc, false);
    final byte[] bytes = b64.binary(info);
    return new ValueIter() {
      final int s = bytes.length;
      int c;
      @Override
      public Int get(final long i) { return Int.get(bytes[(int) i] & 0xFF); }
      @Override
      public Int next() { return c < s ? get(c++) : null; }
      @Override
      public long size() { return s; }
      @Override
      public Value value() { return toValue(bytes); }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return toValue(toB64(exprs[0], qc, false).binary(info));
  }

  /**
   * Returns a value representation of the specified bytes.
   * @param bytes bytes to be wrapped in a value
   * @return value
   */
  private static Value toValue(final byte[] bytes) {
    final int bl = bytes.length;
    final long[] tmp = new long[bl];
    for(int b = 0; b < bl; b++) tmp[b] = bytes[b] & 0xFF;
    return IntSeq.get(tmp, AtomType.ITR);
  }
}
