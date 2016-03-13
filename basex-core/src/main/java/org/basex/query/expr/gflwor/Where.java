package org.basex.query.expr.gflwor;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.GFLWOR.Clause;
import org.basex.query.expr.gflwor.GFLWOR.Eval;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * GFLWOR {@code where} clause, filtering tuples not satisfying the predicate.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Leo Woerteler
 */
public final class Where extends Clause {
  /** Predicate expression. */
  Expr expr;

  /**
   * Constructor.
   * @param expr predicate expression
   * @param info input info
   */
  public Where(final Expr expr, final InputInfo info) {
    super(info);
    this.expr = expr;
  }

  @Override
  Eval eval(final Eval sub) {
    return new Eval() {
      @Override
      public boolean next(final QueryContext qc) throws QueryException {
        while(sub.next(qc)) if(expr.ebv(qc, info).bool(info)) return true;
        return false;
      }
    };
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem();
    expr.plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    return QueryText.WHERE + ' ' + expr;
  }

  @Override
  public boolean has(final Flag flag) {
    return expr.has(flag);
  }

  @Override
  public Where compile(final QueryContext qc, final VarScope scp) throws QueryException {
    expr = expr.compile(qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public Where optimize(final QueryContext qc, final VarScope sc) throws QueryException {
    expr = expr.optimizeEbv(qc, sc);
    if(expr.isValue()) expr = expr.ebv(qc, info);
    return this;
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
  public Clause inline(final QueryContext qc, final VarScope scp, final Var var,
      final Expr ex) throws QueryException {
    final Expr sub = expr.inline(qc, scp, var, ex);
    if(sub == null) return null;
    expr = sub;
    return optimize(qc, scp);
  }

  @Override
  public Where copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new Where(expr.copy(qc, scp, vs), info);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return expr.accept(visitor);
  }

  @Override
  boolean skippable(final Clause cl) {
    // do not slide LET clauses over WHERE (WHERE may filter out many items)
    return !(cl instanceof Let);
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(expr);
  }

  @Override
  void calcSize(final long[] minMax) {
    minMax[0] = 0;
    if(expr == Bln.FALSE) minMax[1] = 0;
  }

  @Override
  public int exprSize() {
    return expr.exprSize();
  }
}
