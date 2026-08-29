package org.basex.query.func.bin;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinUnpackFloat extends BinFn {
  @Override
  public Flt value(final QueryContext qc) throws QueryException {
    return Flt.get(unpack(qc, 4).getFloat());
  }
}
