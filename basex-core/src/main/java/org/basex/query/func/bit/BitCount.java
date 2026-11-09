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
    final Long value = toLongOrNull(arg(0), qc);
    return value == null ? Empty.VALUE : Itr.get(Long.bitCount(value));
  }
}
