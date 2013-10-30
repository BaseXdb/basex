package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Fragment constructor.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class CFrag extends Arr {
  /** Static context. */
  final StaticContext sc;
  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param n name
   */
  CFrag(final StaticContext sctx, final InputInfo ii, final Expr... n) {
    super(ii, n);
    sc = sctx;
    size = 1;
  }

  @Override
  public abstract ANode item(final QueryContext ctx, final InputInfo ii)
      throws QueryException;

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.CNS || super.has(flag);
  }

  /**
   * Returns a string info for the expression.
   * @param pref info prefix
   * @return string
   */
  static String info(final String pref) {
    return pref + " constructor";
  }

  @Override
  protected String toString(final String pref) {
    final StringBuilder sb = new StringBuilder(pref).append(" { ");
    sb.append(expr.length == 0 ? "()" : super.toString(SEP));
    return sb.append(" }").toString();
  }
}
