package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
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

  /**
   * If possible, binds the variable at compile time.
   * @param ctx query context
   * @return result of check
   * @throws QueryException query exception
   */
  protected boolean bind(final QueryContext ctx) throws QueryException {
    // don't bind variable if expression uses variables, context, or fragments
    if(expr.uses(Use.VAR) || expr.uses(Use.CTX) || expr.uses(Use.FRG) ||
        ctx.grouping) return false;

    ctx.compInfo(OPTBIND, var);
    var.bind(expr, ctx);
    return true;
  }

  @Override
  public abstract ForLet comp(final QueryContext ctx) throws QueryException;

  /**
   * Checks if clause has no scoring and position.
   * @return result of check
   */
  abstract boolean simple();

  /**
   * Checks if the clause will shadow the specified variable.
   * @param v variable to be checked
   * @return result of check
   */
  abstract boolean shadows(final Var v);

  @Override
  public final boolean uses(final Use u) {
    return u == Use.VAR || super.uses(u);
  }
}
