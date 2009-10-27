package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;

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
  public Expr comp(final QueryContext ctx) throws QueryException {
    for(final Expr e : expr) checkUp(e, ctx);
    return super.comp(ctx);
  }

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    return u == Use.UPD || super.uses(u, ctx);
  }
}
