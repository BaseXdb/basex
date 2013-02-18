package org.basex.query.gflwor;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.gflwor.GFLWOR.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.InputInfo;
import org.basex.util.hash.*;
import org.basex.util.list.*;


/**
 * GFLWOR {@code where} clause, filtering tuples not satisfying the predicate.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class Where extends GFLWOR.Clause {
  /** Predicate expression. */
  Expr pred;

  /**
   * Constructor.
   * @param ii input info
   * @param e predicate expression
   */
  public Where(final Expr e, final InputInfo ii) {
    super(ii);
    pred = e;
  }

  @Override
  Eval eval(final Eval sub) {
    return new Eval() {
      @Override
      public boolean next(final QueryContext ctx) throws QueryException {
        while(sub.next(ctx)) if(pred.ebv(ctx, info).bool(info)) return true;
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
  public boolean uses(final Use u) {
    return pred.uses(u);
  }

  @Override
  public Where compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    pred = pred.compile(ctx, scp).compEbv(ctx);
    return optimize(ctx, scp);
  }

  @Override
  public Where optimize(final QueryContext cx, final VarScope sc) throws QueryException {
    if(pred.isValue()) pred = pred.ebv(cx, info);
    return this;
  }

  @Override
  public boolean removable(final Var v) {
    return pred.removable(v);
  }

  @Override
  public VarUsage count(final Var v) {
    return pred.count(v);
  }

  @Override
  public GFLWOR.Clause inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    final Expr sub = pred.inline(ctx, scp, v, e);
    if(sub == null) return null;
    pred = sub;
    return optimize(ctx, scp);
  }

  @Override
  public Where copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
    return new Where(pred.copy(ctx, scp, vs), info);
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
  public boolean databases(final StringList db) {
    return pred.databases(db);
  }

  @Override
  long calcSize(final long cnt) {
    return pred == Bln.FALSE ? 0 : -1;
  }

  @Override
  public int exprSize() {
    return pred.exprSize();
  }
}
