package org.basex.query.func.bin;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinOr extends BinFn {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    return bit(Bit.OR, qc);
  }
}
