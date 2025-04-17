package org.basex.query.func.prof;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ProfType extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Expr expr = arg(0);
    final String label = toStringOrNull(arg(1), qc);

    final StringBuilder sb = new StringBuilder().append(info(expr));
    final Value value = expr.value(qc);
    if(expr != value) sb.append(" -> ").append(info(value));
    qc.trace(sb.toString(), label);
    return value;
  }

  /**
   * Returns an info string for the specified expression.
   * @param expr expression
   * @return info string
   */
  private static String info(final Expr expr) {
    return "Type: " + expr.seqType() + ", size: " + expr.size() + ", class: " +
        Util.className(expr);
  }
}
