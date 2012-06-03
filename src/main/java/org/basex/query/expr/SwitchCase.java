package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * Single case of a switch expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class SwitchCase extends Arr {
  /**
   * Constructor.
   * @param ii input info
   * @param e return expression (placed first) and cases
   */
  public SwitchCase(final InputInfo ii, final Expr... e) {
    super(ii, e);
  }

  @Override
  public void checkUp() throws QueryException {
    for(int e = 1; e < expr.length; ++e) checkNoUp(expr[e]);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(int e = 1; e < expr.length; ++e) sb.append(' ' + CASE + ' ' + expr[e]);
    if(expr.length == 1) sb.append(' ' + DEFAULT);
    sb.append(' ' + RETURN + ' ' + expr[0]);
    return sb.toString();
  }
}
