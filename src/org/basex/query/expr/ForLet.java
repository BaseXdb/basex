package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.util.Var;

/**
 * Abstract For/Let Clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class ForLet extends Single {
  /** Variable. */
  protected Var var;

  /**
   * Constructor.
   * @param e variable input
   * @param v variable
   */
  public ForLet(final Expr e, final Var v) {
    super(e);
    var = v;
  }

  /**
   * Checks if clause has no scoring and position.
   * @return result of check
   */
  abstract boolean standard();

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    return u == Use.VAR || super.uses(u, ctx);
  }

  @Override
  public boolean removable(final Var v, final QueryContext ctx) {
    return expr.removable(v, ctx);
  }

  @Override
  public final Expr remove(final Var v) {
    expr = expr.remove(v);
    return this;
  }

  @Override
  public Return returned(final QueryContext ctx) {
    return Return.BLN;
  }

  /**
   * Checks if the specified is not shadowed by another variable.
   * @param v variable to be checked
   * @return result of check
   */
  public boolean shadows(final Var v) {
    return !v.visible(var);
  }

  @Override
  public final String color() {
    return "66CC66";
  }
}
