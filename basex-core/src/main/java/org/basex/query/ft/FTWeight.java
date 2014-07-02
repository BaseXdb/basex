package org.basex.query.ft;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * FTOptions expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FTWeight extends FTExpr {
  /** Weight. */
  private Expr weight;

  /**
   * Constructor.
   * @param info input info
   * @param expr expression
   * @param weight weight
   */
  public FTWeight(final InputInfo info, final FTExpr expr, final Expr weight) {
    super(info, expr);
    this.weight = weight;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(weight);
    super.checkUp();
  }

  @Override
  public FTExpr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    weight = weight.compile(ctx, scp);
    return super.compile(ctx, scp);
  }

  // called by sequential variant
  @Override
  public FTNode item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    return weight(exprs[0].item(ctx, info), ctx);
  }

  // called by index variant
  @Override
  public FTIter iter(final QueryContext ctx) {
    return new FTIter() {
      @Override
      public FTNode next() throws QueryException {
        return weight(exprs[0].iter(ctx).next(), ctx);
      }
    };
  }

  /**
   * Returns the item with weight calculation.
   * @param item input item
   * @param ctx query context
   * @return item
   * @throws QueryException query exception
   */
  private FTNode weight(final FTNode item, final QueryContext ctx) throws QueryException {
    // evaluate weight
    if(item == null) return null;
    final double d = checkDbl(weight, ctx);
    if(Math.abs(d) > 1000) throw FTWEIGHT.get(info, d);
    if(d == 0) item.all.size(0);
    item.score(item.score() * d);
    return item;
  }

  @Override
  public boolean indexAccessible(final IndexCosts ic) {
    // weight makes no sense as long as no index-based scoring exists
    return false;
  }

  @Override
  public boolean has(final Flag flag) {
    return weight.has(flag) || super.has(flag);
  }

  @Override
  public boolean removable(final Var v) {
    return weight.removable(v) && super.removable(v);
  }

  @Override
  public VarUsage count(final Var v) {
    return weight.count(v).plus(super.count(v));
  }

  @Override
  public FTExpr inline(final QueryContext ctx, final VarScope scp, final Var v, final Expr e)
      throws QueryException {
    boolean change = inlineAll(ctx, scp, exprs, v, e);
    final Expr w = weight.inline(ctx, scp, v, e);
    if(w != null) {
      weight = w;
      change = true;
    }
    return change ? optimize(ctx, scp) : null;
  }

  @Override
  public FTExpr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new FTWeight(info, exprs[0].copy(ctx, scp, vs), weight.copy(ctx, scp, vs));
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), weight, exprs[0]);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return super.accept(visitor) && weight.accept(visitor);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final FTExpr e : exprs) sz += e.exprSize();
    return sz + weight.exprSize();
  }

  @Override
  public String toString() {
    return exprs[0] + " " + QueryText.WEIGHT + ' ' + weight;
  }
}
