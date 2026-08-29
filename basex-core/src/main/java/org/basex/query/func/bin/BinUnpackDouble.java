package org.basex.query.func.bin;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinUnpackDouble extends BinFn {
  @Override
  public Dbl value(final QueryContext qc) throws QueryException {
    return Dbl.get(unpack(qc, 8).getDouble());
  }
}
