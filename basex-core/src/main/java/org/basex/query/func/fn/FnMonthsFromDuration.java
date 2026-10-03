package org.basex.query.func.fn;

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
public final class FnMonthsFromDuration extends DateTimeFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Dur value = toDurOrNull(arg(0), qc);
    return value == null ? Empty.VALUE : Itr.get(value.mon());
  }
}
