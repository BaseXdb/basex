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
    final Item item = atomItem(arg(0), qc);
    if(item == Empty.VALUE) return Empty.VALUE;

    final long value = toLong(item, arg(0)), index = toLong(atomItem(arg(1), qc), 0);
    return Bln.get((value >> Math.min(index, Long.SIZE - 1) & 1) != 0);
  }
}
