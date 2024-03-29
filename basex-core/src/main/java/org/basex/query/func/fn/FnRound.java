package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class FnRound extends NumericFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return round(qc, false);
  }

  /**
   * Rounds values.
   * @param qc query context
   * @param even half-to-even flag
   * @return number or {@link Empty#VALUE}
   * @throws QueryException query exception
   */
  final Item round(final QueryContext qc, final boolean even) throws QueryException {
    final ANum value = toNumberOrNull(arg(0), qc);
    final Item precision = arg(1).atomItem(qc, info);

    final long prec = precision.isEmpty() ? 0 : Math.max(Integer.MIN_VALUE, toLong(precision));
    return value == null ? Empty.VALUE : prec > Integer.MAX_VALUE ? value :
      value.round((int) prec, even);
  }
}
