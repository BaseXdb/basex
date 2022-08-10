package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FnMonthFromDateTime extends DateTime {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item value = exprs[0].atomItem(qc, info);
    return value == Empty.VALUE ? Empty.VALUE :
      Int.get(toDate(value, AtomType.DATE_TIME, qc).mon());
  }
}
