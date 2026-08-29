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
public final class FnMinutesFromTime extends DateTimeFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Item value = arg(0).atomItem(qc, info);
    if(value.isEmpty()) return Empty.VALUE;

    return Itr.get(toDate(value, BasicType.TIME, qc).minute());
  }
}
