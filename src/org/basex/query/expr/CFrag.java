package org.basex.query.expr;

import org.basex.query.QueryContext;

/**
 * Fragment constructor.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class CFrag extends Arr {
  /**
   * Constructor.
   * @param n name
   */
  public CFrag(final Expr... n) {
    super(n);
  }

  @Override
  public final boolean uses(final Use u, final QueryContext ctx) {
    return u == Use.FRG || super.uses(u, ctx);
  }

  @Override
  public final Return returned(final QueryContext ctx) {
    return Return.NOD;
  }

  /**
   * Returns a string representation of the expression.
   * @param pre expression prefix
   * @return string
   */
  protected final String toString(final String pre) {
    return pre + " { " + super.toString(", ") + " }";
  }
}
