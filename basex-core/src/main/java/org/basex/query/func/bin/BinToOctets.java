package org.basex.query.func.bin;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.convert.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinToOctets extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Bin binary = toBin(arg(0), qc);
    final byte[] bytes = binary.binary(info);

    return new BasicIter<Int>(bytes.length) {
      @Override
      public Int get(final long i) {
        return Int.get(bytes[(int) i] & 0xFF);
      }
      @Override
      public Value value(final QueryContext q, final Expr expr) {
        return ConvertBinaryToIntegers.toValue(bytes);
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return ConvertBinaryToIntegers.toValue(toBin(arg(0), qc).binary(info));
  }
}
