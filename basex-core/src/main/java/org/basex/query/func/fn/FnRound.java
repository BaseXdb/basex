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
 * @author BaseX Team 2005-21, BSD License
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
    final ANum num = toNumberOrNull(exprs[0], qc);
    final long p = exprs.length == 1 ? 0 : Math.max(Integer.MIN_VALUE, toLong(exprs[1], qc));
    return num == null ? Empty.VALUE : p > Integer.MAX_VALUE ? num : num.round((int) p, even);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst();
  }
}
