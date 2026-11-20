package org.basex.query.func.bit;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BitTest extends BitFn {
  @Override
  Value value(final long value, final QueryContext qc) throws QueryException {
    final long index = toLong(arg(1), 0, qc);
    return Bln.get((value >> Math.min(index, Long.SIZE - 1) & 1) != 0);
  }
}
