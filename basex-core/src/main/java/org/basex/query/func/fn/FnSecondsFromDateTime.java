package org.basex.query.func.fn;

import java.math.*;

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
public final class FnSecondsFromDateTime extends DateTime {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item value = arg(0).atomItem(qc, info);
    if(value.isEmpty()) return Empty.VALUE;

    final BigDecimal comp = toGregorian(value, qc).sec();
    return comp == null ? Empty.VALUE : Dec.get(comp);
  }
}
