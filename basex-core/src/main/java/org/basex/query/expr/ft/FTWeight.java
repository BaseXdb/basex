package org.basex.query.expr.ft;

import static org.basex.query.QueryError.*;

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
  public FTExpr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    weight = weight.compile(qc, scp);
    return super.compile(qc, scp);
  }

  // called by sequential variant
  @Override
  public FTNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return weight(exprs[0].item(qc, info), qc);
  }

  // called by index variant
  @Override
  public FTIter iter(final QueryContext qc) {
    return new FTIter() {
      @Override
      public FTNode next() throws QueryException {
        return weight(exprs[0].iter(qc).next(), qc);
      }
    };
  }

  /**
   * Returns the item with weight calculation.
   * @param item input item
   * @param qc query context
   * @return item
   * @throws QueryException query exception
   */
  private FTNode weight(final FTNode item, final QueryContext qc) throws QueryException {
    // evaluate weight
    if(item == null) return null;
    final double d = toDouble(weight, qc);
    if(Math.abs(d) > 1000) throw FTWEIGHT_X.get(info, d);
    if(d == 0) item.all.size(0);
    item.score(item.score() * d);
    return item;
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) {
    // weight makes no sense as long as no index-based scoring exists
    return false;
  }

  @Override
  public boolean has(final Flag flag) {
    return weight.has(flag) || super.has(flag);
  }

  @Override
  public boolean removable(final Var var) {
    return weight.removable(var) && super.removable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    return weight.count(var).plus(super.count(var));
  }

  @Override
  public FTExpr inline(final QueryContext qc, final VarScope scp, final Var var, final Expr ex)
      throws QueryException {
    boolean change = inlineAll(qc, scp, exprs, var, ex);
    final Expr w = weight.inline(qc, scp, var, ex);
    if(w != null) {
      weight = w;
      change = true;
    }
    return change ? optimize(qc, scp) : null;
  }

  @Override
  public FTExpr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new FTWeight(info, exprs[0].copy(qc, scp, vs), weight.copy(qc, scp, vs));
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
