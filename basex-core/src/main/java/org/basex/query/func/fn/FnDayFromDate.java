package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnDayFromDate extends DateTimeFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ADate value = toDateOrNull(arg(0), BasicType.DATE, qc);
    return value == null ? Empty.VALUE : Itr.get(value.day());
  }
}
