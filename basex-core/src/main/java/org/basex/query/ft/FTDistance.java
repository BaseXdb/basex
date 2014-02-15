package org.basex.query.ft;

import static org.basex.query.QueryText.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTDistance expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FTDistance extends FTFilter {
  /** Minimum distance. */
  private Expr min;
  /** Maximum distance. */
  private Expr max;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param mn minimum
   * @param mx maximum
   * @param u unit
   */
  public FTDistance(final InputInfo ii, final FTExpr e, final Expr mn, final Expr mx,
      final FTUnit u) {
    super(ii, e, u);
    min = mn;
    max = mx;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(min, max);
    super.checkUp();
  }

  @Override
  public FTExpr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    min = min.compile(ctx, scp);
    max = max.compile(ctx, scp);
    return super.compile(ctx, scp);
  }

  @Override
  protected boolean filter(final QueryContext ctx, final FTMatch mtc, final FTLexer lex)
      throws QueryException {

    final long mn = checkItr(min, ctx);
    final long mx = checkItr(max, ctx);
    mtc.sort();

    final FTMatch match = new FTMatch();
    FTStringMatch last = null, first = null;
    for(final FTStringMatch sm : mtc) {
      if(sm.exclude) {
        match.add(sm);
      } else {
        if(first == null) {
          first = sm;
        } else {
          final int d = pos(sm.start, lex) - pos(last.end, lex) - 1;
          if(d < mn || d > mx) return false;
        }
        last = sm;
      }
    }
    first.end = last.end;
    mtc.reset();
    mtc.add(first);
    mtc.add(match);
    return true;
  }

  @Override
  public boolean has(final Flag flag) {
    return min.has(flag) || max.has(flag) || super.has(flag);
  }

  @Override
  public boolean removable(final Var v) {
    return min.removable(v) || max.removable(v) && super.removable(v);
  }

  @Override
  public VarUsage count(final Var v) {
    return super.count(v).plus(VarUsage.sum(v, min, max));
  }

  @Override
  public FTExpr inline(final QueryContext ctx, final VarScope scp, final Var v, final Expr e)
      throws QueryException {
    return inlineAll(ctx, scp, expr, v, e) || inlineAll(ctx, scp, new Expr[] { min, max }, v, e)
        ? optimize(ctx, scp) : null;
  }

  @Override
  public FTExpr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new FTDistance(info, expr[0].copy(ctx, scp, vs),
        min.copy(ctx, scp, vs), max.copy(ctx, scp, vs), unit);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(DISTANCE, min + "-" + max + ' ' + unit), expr);
  }

  @Override
  public String toString() {
    return super.toString() + DISTANCE + PAR1 + min + '-' + max + ' ' + unit + PAR2;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return super.accept(visitor) && visitAll(visitor, min, max);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final FTExpr e : expr) sz += e.exprSize();
    return min.exprSize() + max.exprSize() + sz;
  }
}
