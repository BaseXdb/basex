package org.basex.query.up;

import org.basex.query.QueryContext;
import org.basex.query.expr.Arr;
import org.basex.query.expr.Expr;

/**
 * Abstract update expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class Update extends Arr {
  /**
   * Constructor.
   * @param e expressions
   */
  protected Update(final Expr... e) {
    super(e);
  }

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    return u == Use.UPD || super.uses(u, ctx);
  }
}
