package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Abstract single expression.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public abstract class Single extends ParseExpr {
  /** Expression. */
  public Expr expr;

  /**
   * Constructor.
   * @param info input info
   * @param expr expression
   * @param seqType sequence type
   */
  protected Single(final InputInfo info, final Expr expr, final SeqType seqType) {
    super(info, seqType);
    this.expr = expr;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(expr);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    expr = expr.compile(cc);
    return this;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    return expr.isValue() ? cc.preEval(this) : this;
  }

  @Override
  public boolean has(final Flag... flags) {
    return expr.has(flags);
  }

  @Override
  public boolean removable(final Var var) {
    return expr.removable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    return expr.count(var);
  }

  @Override
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    final Expr sub = expr.inline(var, ex, cc);
    if(sub == null) return null;
    expr = sub;
    return optimize(cc);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return expr.accept(visitor);
  }

  @Override
  public int exprSize() {
    return expr.exprSize() + 1;
  }

  /**
   * {@inheritDoc}
   * Must be overwritten by implementing class.
   */
  @Override
  public boolean equals(final Object obj) {
    return obj instanceof Single && expr.equals(((Single) obj).expr);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), expr);
  }
}
