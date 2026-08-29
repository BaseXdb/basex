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
public final class FnMinutesFromDateTime extends DateTimeFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ADate value = toGregorianOrNull(arg(0), qc);
    if(value == null) return Empty.VALUE;

    return value.hasMinutes() ? Itr.get(value.minute()) : Empty.VALUE;
  }

  @Override
  protected boolean mayBeEmpty() {
    return true;
  }
}
