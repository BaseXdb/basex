package org.basex.query.func.bin;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinPadRight extends BinFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return pad(qc, false);
  }
}
