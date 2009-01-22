package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.iter.ResetIter;
import org.basex.query.util.Var;

/**
 * Abstract For/Let Clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class ForLet extends Expr {
  /** Variable inputs. */
  protected Expr expr;
  /** Variable. */
  protected Var var;

  @Override
  public final boolean usesVar(final Var v) {
    return v == null || expr.usesVar(v);
  }

  @Override
  public final Expr removeVar(final Var v) {
    expr = expr.removeVar(v);
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

  @Override
  public abstract ResetIter iter(final QueryContext ctx);
}
