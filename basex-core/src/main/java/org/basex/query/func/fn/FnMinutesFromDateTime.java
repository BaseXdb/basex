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
public final class FnMinutesFromDateTime extends DateTimeFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ADate value = toGregorianOrNull(arg(0), qc);
    if(value == null) return Empty.VALUE;

    final long comp = value.minute();
    return comp == -1 ? Empty.VALUE : Itr.get(comp);
  }
}
