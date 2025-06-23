package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnYearFromDateTime extends DateTimeFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item value = arg(0).atomItem(qc, info);
    if(value.isEmpty()) return Empty.VALUE;

    final long comp = toGregorian(value, qc).yea();
    return comp == Long.MAX_VALUE ? Empty.VALUE : Itr.get(comp);
  }
}
