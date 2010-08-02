package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.SeqType;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Abstract For/Let Clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class ForLet extends Single {
  /** Variable. */
  public final Var var;

  /**
   * Constructor.
   * @param ii input info
   * @param e variable input
   * @param v variable
   */
  protected ForLet(final InputInfo ii, final Expr e, final Var v) {
    super(ii, e);
    var = v;
  }

  @Override
  public abstract ForLet comp(final QueryContext ctx) throws QueryException;

  /**
   * Checks if clause has no scoring and position.
   * @return result of check
   */
  abstract boolean simple();

  @Override
  public final boolean uses(final Use u, final QueryContext ctx) {
    return u == Use.VAR || super.uses(u, ctx);
  }

  @Override
  public final boolean removable(final Var v, final QueryContext ctx) {
    return expr.removable(v, ctx);
  }

  @Override
  public final Expr remove(final Var v) {
    expr = expr.remove(v);
    return this;
  }

  @Override
  public final SeqType returned(final QueryContext ctx) {
    return SeqType.BLN_ZM;
  }

  /**
   * Checks if the specified is not shadowed by another variable.
   * @param v variable to be checked
   * @return result of check
   */
  boolean shadows(final Var v) {
    return !v.visible(var);
  }

  @Override
  public final String color() {
    return "66CC66";
  }
}
