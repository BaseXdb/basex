package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.util.*;

/**
 * Fragment constructor.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class CFrag extends Arr {
  /**
   * Constructor.
   * @param ii input info
   * @param n name
   */
  CFrag(final InputInfo ii, final Expr... n) {
    super(ii, n);
    type = SeqType.NOD;
  }

  @Override
  public abstract ANode item(final QueryContext ctx, final InputInfo ii)
      throws QueryException;

  @Override
  public boolean uses(final Use u) {
    return u == Use.CNS || super.uses(u);
  }

  /**
   * Returns a string info for the expression.
   * @param pref info prefix
   * @return string
   */
  static final String info(final String pref) {
    return pref + " constructor";
  }

  @Override
  protected String toString(final String pref) {
    final StringBuilder sb = new StringBuilder(pref).append(" { ");
    sb.append(expr.length == 0 ? "()" : super.toString(SEP));
    return sb.append(" }").toString();
  }
}
