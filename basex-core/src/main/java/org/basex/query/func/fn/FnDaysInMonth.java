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
public final class FnDaysInMonth extends DateTimeFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Item value = atomItem(arg(0), qc);
    if(value.isEmpty()) return Empty.VALUE;

    final ADate date = (ADate) coerce(value, Types.YEAR_MONTH_ZO, arg(0), qc);
    return Itr.get(ADate.daysOfMonth(date.yea(), (int) date.mon()));
  }
}
