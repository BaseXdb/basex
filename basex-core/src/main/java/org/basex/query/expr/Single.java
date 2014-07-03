package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Abstract single expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class Single extends ParseExpr {
  /** Expression. */
  public Expr expr;

  /**
   * Constructor.
   * @param info input info
   * @param expr expression
   */
  protected Single(final InputInfo info, final Expr expr) {
    super(info);
    this.expr = expr;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(expr);
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    expr = expr.compile(qc, scp);
    return this;
  }

  @Override
  public boolean has(final Flag flag) {
    return expr.has(flag);
  }

  @Override
  public boolean removable(final Var v) {
    return expr.removable(v);
  }

  @Override
  public VarUsage count(final Var v) {
    return expr.count(v);
  }

  @Override
  public Expr inline(final QueryContext qc, final VarScope scp, final Var v, final Expr e)
      throws QueryException {
    final Expr sub = expr.inline(qc, scp, v, e);
    if(sub == null) return null;
    expr = sub;
    return optimize(qc, scp);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), expr);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return expr.accept(visitor);
  }

  @Override
  public int exprSize() {
    return expr.exprSize() + 1;
  }
}
