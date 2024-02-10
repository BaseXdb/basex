package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnSecondsFromTime extends DateTime {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item value = arg(0).atomItem(qc, info);
    return value.isEmpty() ? Empty.VALUE : Dec.get(toDate(value, AtomType.TIME, qc).sec());
  }
}
