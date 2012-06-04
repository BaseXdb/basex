package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Abstract single expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class Single extends ParseExpr {
  /** Expression. */
  public Expr expr;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   */
  protected Single(final InputInfo ii, final Expr e) {
    super(ii);
    expr = e;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(expr);
  }

  @Override
  public Expr compile(final QueryContext ctx) throws QueryException {
    expr = expr.compile(ctx);
    return this;
  }

  @Override
  public boolean uses(final Use u) {
    return expr.uses(u);
  }

  @Override
  public int count(final Var v) {
    return expr.count(v);
  }

  @Override
  public boolean removable(final Var v) {
    return expr.removable(v);
  }

  @Override
  public Expr remove(final Var v) {
    expr = expr.remove(v);
    return this;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), expr);
  }
}
