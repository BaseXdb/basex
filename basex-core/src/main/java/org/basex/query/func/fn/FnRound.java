package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class FnRound extends StandardFunc {
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
    final ANum value = toNumberOrNull(exprs[0], qc);
    final long prec = defined(1) ? Math.max(Integer.MIN_VALUE, toLong(exprs[1], qc)) : 0;
    return value == null ? Empty.VALUE : prec > Integer.MAX_VALUE ? value :
      value.round((int) prec, even);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst();
  }
}
