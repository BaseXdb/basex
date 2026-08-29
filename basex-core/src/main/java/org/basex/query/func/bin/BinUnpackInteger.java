package org.basex.query.func.bin;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinUnpackInteger extends BinFn {
  @Override
  public Itr value(final QueryContext qc) throws QueryException {
    return unpackInteger(qc, true);
  }
}
