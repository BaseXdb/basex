package org.basex.query.expr.ft;

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
   * @param info input info
   * @param expr expression
   * @param min minimum
   * @param max maximum
   * @param unit unit
   */
  public FTDistance(final InputInfo info, final FTExpr expr, final Expr min, final Expr max,
      final FTUnit unit) {
    super(info, expr, unit);
    this.min = min;
    this.max = max;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(min, max);
    super.checkUp();
  }

  @Override
  public FTExpr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    min = min.compile(qc, scp);
    max = max.compile(qc, scp);
    return super.compile(qc, scp);
  }

  @Override
  protected boolean filter(final QueryContext qc, final FTMatch mtc, final FTLexer lex)
      throws QueryException {

    final long mn = toLong(min, qc), mx = toLong(max, qc);
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
  public boolean removable(final Var var) {
    return min.removable(var) || max.removable(var) && super.removable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    return super.count(var).plus(VarUsage.sum(var, min, max));
  }

  @Override
  public FTExpr inline(final QueryContext qc, final VarScope scp, final Var var, final Expr ex)
      throws QueryException {
    final Expr mn = min.inline(qc, scp, var, ex), mx = max.inline(qc, scp, var, ex);
    if(mn != null) min = mn;
    if(mx != null) max = mx;

    return inlineAll(qc, scp, exprs, var, ex) || mn != null || mx != null
        ? optimize(qc, scp) : null;
  }

  @Override
  public FTExpr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new FTDistance(info, exprs[0].copy(qc, scp, vs),
        min.copy(qc, scp, vs), max.copy(qc, scp, vs), unit);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(DISTANCE, min + "-" + max + ' ' + unit), exprs);
  }

  @Override
  public String toString() {
    return super.toString() + DISTANCE + PAREN1 + min + '-' + max + ' ' + unit + PAREN2;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return super.accept(visitor) && visitAll(visitor, min, max);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final FTExpr e : exprs) sz += e.exprSize();
    return min.exprSize() + max.exprSize() + sz;
  }
}
