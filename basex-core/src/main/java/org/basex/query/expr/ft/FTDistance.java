package org.basex.query.expr.ft;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.ft.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTDistance expression.
 *
 * @author BaseX Team 2005-21, BSD License
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
    return super.compile(cc).optimize(cc);
  }

  @Override
  public FTExpr optimize(final CompileContext cc) throws QueryException {
    min = min.simplifyFor(Simplify.NUMBER, cc);
    max = max.simplifyFor(Simplify.NUMBER, cc);
    return this;
  }

  @Override
  protected boolean filter(final QueryContext qc, final FTMatch match, final FTLexer lexer)
      throws QueryException {

    final long mn = toLong(min, qc), mx = toLong(max, qc);
    match.sort();

    final FTMatch ftm = new FTMatch();
    FTStringMatch first = null, last = null;
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
    if(first == null) return false;

    first.end = last.end;
    match.reset();
    match.add(first);
    match.add(ftm);
    return true;
  }

  @Override
  public boolean has(final Flag... flags) {
    return min.has(flags) || max.has(flags) || super.has(flags);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    return min.inlineable(ic) || max.inlineable(ic) && super.inlineable(ic);
  }

  @Override
  public VarUsage count(final Var var) {
    return super.count(var).plus(VarUsage.sum(var, min, max));
  }

  @Override
  public FTExpr inline(final InlineContext ic) throws QueryException {
    final Expr mn = min.inline(ic), mx = max.inline(ic);
    if(mn != null) min = mn;
    if(mx != null) max = mx;
    return ic.inline(exprs) || mn != null || mx != null ? optimize(ic.cc) : null;
  }

  @Override
  public FTExpr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new FTDistance(info, exprs[0].copy(cc, vm), min.copy(cc, vm), max.copy(cc, vm),
        unit));
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return super.accept(visitor) && visitAll(visitor, min, max);
  }

  @Override
  public int exprSize() {
    int size = 1;
    for(final FTExpr expr : exprs) size += expr.exprSize();
    return min.exprSize() + max.exprSize() + size;
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this, DISTANCE, min + "-" + max + ' ' + unit), exprs);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(exprs[0]).token(DISTANCE).paren(min + "-" + max + ' ' + unit);
  }
}
