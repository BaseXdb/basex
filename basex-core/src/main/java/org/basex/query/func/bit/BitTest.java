package org.basex.query.func.bit;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BitTest extends BitFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Long value = toLongOrNull(arg(0), qc);
    if(value == null) return Empty.VALUE;

    final long index = toLong(arg(1).atomItem(qc, info), 0);
    return Bln.get((value >> Math.min(index, Long.SIZE - 1) & 1) != 0);
  }
}
