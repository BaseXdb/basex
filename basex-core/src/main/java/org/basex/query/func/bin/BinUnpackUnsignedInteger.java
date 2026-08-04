package org.basex.query.func.bin;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinUnpackUnsignedInteger extends BinFn {
  @Override
  protected Itr item(final QueryContext qc) throws QueryException {
    return unpackInteger(qc, false);
  }
}
