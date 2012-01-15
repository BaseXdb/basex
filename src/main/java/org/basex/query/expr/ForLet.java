package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Abstract For/Let Clause.
 *
 * @author BaseX Team 2005-12, BSD License
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
   * @throws QueryException query exception
   */
  final void bind(final QueryContext ctx) throws QueryException {
    if(!simple(true)) return;

    /* don't bind variable if expression...
       - has free variables, eg:
         for $a in 1 to 2 let $b := $a return $b
       - is non-deterministic, eg:
         let $x := random() return ($x, $x)
       - creates fragments, eg:
         let $x := <x/> return $x is $x
       - depends on context, eg:
         (<a/>,<b/>)/(let $a := position() return $a=last())
     */
    if(expr.hasFreeVars(ctx) || expr.uses(Use.NDT) || expr.uses(Use.CTX) ||
        expr.uses(Use.CNS) || ctx.grouping) return;

    ctx.compInfo(OPTBIND, var);
    var.bind(expr, ctx);
  }

  @Override
  public abstract ForLet comp(final QueryContext ctx) throws QueryException;

  /**
   * Checks if the clause contains a simple variable declaration, using
   * no scoring and no positioning.
   * @param one clause must not return more than one value
   * @return result of check
   */
  abstract boolean simple(final boolean one);

  /**
   * Returns the total number of occurrences of all variables that are
   * defined in the specified clause.
   * @param fl clause to be checked
   * @return total number of occurrences
   */
  final int count(final ForLet fl) {
    int c = expr.count(fl.var);
    if(fl instanceof For) {
      final For f = (For) fl;
      if(f.pos != null) c += expr.count(f.pos);
      if(f.score != null) c += expr.count(f.score);
    }
    return c;
  }

  @Override
  public final boolean uses(final Use u) {
    return u == Use.VAR || super.uses(u);
  }

  /**
   * Checks if the given variable is declared by this clause.
   * @param v variable
   * @return declaration flag
   */
  public abstract boolean declares(final Var v);

  /**
   * Gathers all variables declared by this clause.
   * @return variables
   */
  public abstract Var[] vars();
}
