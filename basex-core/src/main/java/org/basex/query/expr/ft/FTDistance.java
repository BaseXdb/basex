package org.basex.query.expr.ft;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.ft.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTDistance expression.
 *
 * @author BaseX Team 2005-17, BSD License
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
  public FTExpr compile(final CompileContext cc) throws QueryException {
    min = min.compile(cc);
    max = max.compile(cc);
    return super.compile(cc);
  }

  @Override
  protected boolean filter(final QueryContext qc, final FTMatch match, final FTLexer lexer)
      throws QueryException {

    final long mn = toLong(min, qc), mx = toLong(max, qc);
    match.sort();

    final FTMatch ftm = new FTMatch();
    FTStringMatch last = null, first = null;
    for(final FTStringMatch sm : match) {
      if(sm.exclude) {
        ftm.add(sm);
      } else {
        if(first == null) {
          first = sm;
        } else {
          final int d = pos(sm.start, lexer) - pos(last.end, lexer) - 1;
          if(d < mn || d > mx) return false;
        }
        last = sm;
      }
    }
    first.end = last.end;
    match.reset();
    match.add(first);
    match.add(ftm);
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
  public FTExpr inline(final Var var, final Expr ex, final CompileContext cc)
      throws QueryException {
    final Expr mn = min.inline(var, ex, cc), mx = max.inline(var, ex, cc);
    if(mn != null) min = mn;
    if(mx != null) max = mx;
    return inlineAll(exprs, var, ex, cc) || mn != null || mx != null ? optimize(cc) : null;
  }

  @Override
  public FTExpr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new FTDistance(info, exprs[0].copy(cc, vm),
        min.copy(cc, vm), max.copy(cc, vm), unit);
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
    for(final FTExpr expr : exprs) sz += expr.exprSize();
    return min.exprSize() + max.exprSize() + sz;
  }
}
