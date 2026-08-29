package org.basex.query.func.bin;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinXor extends BinFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return bit(Bit.XOR, qc);
  }
}
