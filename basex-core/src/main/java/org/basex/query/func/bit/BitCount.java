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
public final class BitCount extends BitFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Item item = atomItem(arg(0), qc);
    return item == Empty.VALUE ? Empty.VALUE : Itr.get(Long.bitCount(toLong(item, arg(0))));
  }
}
