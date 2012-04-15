package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;

/**
 * Single case of a switch expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class SwitchCase extends Arr {
  /**
   * Constructor.
   * @param ii input info
   * @param e return expression (placed first) and cases
   */
  public SwitchCase(final InputInfo ii, final Expr... e) {
    super(ii, e);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    final int el = expr.length;
    expr[0] = expr[0].comp(ctx);
    for(int e = 1; e < el; ++e) expr[e] = checkUp(expr[e], ctx).comp(ctx);
    return this;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    final int el = expr.length;
    for(int e = 1; e < el; ++e) sb.append(' ' + CASE + ' ' + expr[e]);
    if(el == 1) sb.append(' ' + DEFAULT);
    sb.append(' ' + RETURN + ' ' + expr[0]);
    return sb.toString();
  }
}
