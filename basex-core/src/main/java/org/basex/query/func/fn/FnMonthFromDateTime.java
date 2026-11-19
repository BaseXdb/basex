package org.basex.query.func.fn;

import static org.basex.query.value.type.BasicType.*;

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
public final class FnMonthFromDateTime extends DateTimeFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ADate value = toGregorianOrNull(arg(0), qc);
    if(value == null) return Empty.VALUE;

    return value.hasMonth() ? Itr.get(value.mon()) : Empty.VALUE;
  }

  @Override
  protected boolean mayBeEmpty() {
    return !argType(DATE_TIME, DATE, G_YEAR_MONTH, G_MONTH, G_MONTH_DAY);
  }
}
