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
import org.basex.util.list.*;

/**
 * FTOptions expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FTWeight extends FTExpr {
  /** Weight. */
  private Expr weight;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param w weight
   */
  public FTWeight(final InputInfo ii, final FTExpr e, final Expr w) {
    super(ii, e);
    weight = w;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(weight);
    super.checkUp();
  }

  @Override
  public FTExpr compile(final QueryContext ctx, final VarScope scp)
      throws QueryException {
    weight = weight.compile(ctx, scp);
    return super.compile(ctx, scp);
  }

  // called by sequential variant
  @Override
  public FTNode item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    return weight(expr[0].item(ctx, info), ctx);
  }

  // called by index variant
  @Override
  public FTIter iter(final QueryContext ctx) {
    return new FTIter() {
      @Override
      public FTNode next() throws QueryException {
        return weight(expr[0].iter(ctx).next(), ctx);
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
  FTNode weight(final FTNode item, final QueryContext ctx) throws QueryException {
    // evaluate weight
    if(item == null) return null;
    final double d = checkDbl(weight, ctx);
    if(Math.abs(d) > 1000) FTWEIGHT.thrw(info, d);
    if(d == 0) item.all.size = 0;
    item.score(item.score() * d);
    return item;
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) {
    // weight makes no sense as long as no index-based scoring exists
    return false;
  }

  @Override
  public boolean uses(final Use u) {
    return weight.uses(u) || super.uses(u);
  }

  @Override
  public boolean removable(final Var v) {
    return weight.removable(v) && super.removable(v);
  }

  @Override
  public FTExpr remove(final Var v) {
    weight = weight.remove(v);
    return super.remove(v);
  }

  @Override
  public VarUsage count(final Var v) {
    return weight.count(v).plus(super.count(v));
  }

  @Override
  public FTExpr inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    boolean change = inlineAll(ctx, scp, expr, v, e);
    final Expr w = weight.inline(ctx, scp, v, e);
    if(w != null) {
      weight = w;
      change = true;
    }
    return change ? optimize(ctx, scp) : null;
  }

  @Override
  public FTExpr copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
    return new FTWeight(info, expr[0].copy(ctx, scp, vs), weight.copy(ctx, scp, vs));
  }

  @Override
  public boolean databases(final StringList db) {
    return weight.databases(db) && super.databases(db);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), weight, expr[0]);
  }

  @Override
  public String toString() {
    return expr[0] + " " + QueryText.WEIGHT + ' ' + weight;
  }

  @Override
  public boolean visitVars(final VarVisitor visitor) {
    return visitor.visitAll(expr) && weight.visitVars(visitor);
  }
}
