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
public final class FnSecondsFromDateTime extends DateTimeFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ADate value = toGregorianOrNull(arg(0), qc);
    if(value == null) return Empty.VALUE;

    final BigDecimal comp = value.sec();
    return comp == null ? Empty.VALUE : Dec.get(comp);
  }
}
