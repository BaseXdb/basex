package org.basex.query.gflwor;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.gflwor.GFLWOR.Eval;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;


/**
 * GFLWOR {@code where} clause, filtering tuples not satisfying the predicate.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class Where extends GFLWOR.Clause {
  /** Predicate expression. */
  Expr pred;

  /**
   * Constructor.
   * @param pred predicate expression
   * @param info input info
   */
  public Where(final Expr pred, final InputInfo info) {
    super(info);
    this.pred = pred;
  }

  @Override
  Eval eval(final Eval sub) {
    return new Eval() {
      @Override
      public boolean next(final QueryContext qc) throws QueryException {
        while(sub.next(qc)) if(pred.ebv(qc, info).bool(info)) return true;
        return false;
      }
    };
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem();
    pred.plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    return QueryText.WHERE + ' ' + pred;
  }

  @Override
  public boolean has(final Flag flag) {
    return pred.has(flag);
  }

  @Override
  public Where compile(final QueryContext qc, final VarScope scp) throws QueryException {
    pred = pred.compile(qc, scp).compEbv(qc);
    return optimize(qc, scp);
  }

  @Override
  public Where optimize(final QueryContext qc, final VarScope sc) throws QueryException {
    if(pred.isValue()) pred = pred.ebv(qc, info);
    return this;
  }

  @Override
  public boolean removable(final Var var) {
    return pred.removable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    return pred.count(var);
  }

  @Override
  public GFLWOR.Clause inline(final QueryContext qc, final VarScope scp, final Var var,
      final Expr ex) throws QueryException {
    final Expr sub = pred.inline(qc, scp, var, ex);
    if(sub == null) return null;
    pred = sub;
    return optimize(qc, scp);
  }

  @Override
  public Where copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new Where(pred.copy(qc, scp, vs), info);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return pred.accept(visitor);
  }

  @Override
  boolean skippable(final GFLWOR.Clause cl) {
    return true;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(pred);
  }

  @Override
  void calcSize(final long[] minMax) {
    minMax[0] = 0;
    if(pred == Bln.FALSE) minMax[1] = 0;
  }

  @Override
  public int exprSize() {
    return pred.exprSize();
  }
}
