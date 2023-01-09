package org.basex.query.expr.ft;

import static org.basex.query.QueryText.*;

import java.util.*;

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
 * @author BaseX Team 2005-23, BSD License
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

    // create all possible combinations
    final FTMatch includes = new FTMatch(), excludes = new FTMatch();
    for(final FTStringMatch sm : match) (sm.exclude ? excludes : includes).add(sm);
    int pos = -1;
    final ArrayList<FTMatch> matches = new ArrayList<>();
    for(final FTStringMatch include : includes) {
      if(pos < include.pos) {
        pos = include.pos;
        if(matches.isEmpty()) {
          matches.add(new FTMatch().add(include));
        } else {
          for(final FTMatch ftm : matches) ftm.add(include);
        }
      } else {
        final int ms = matches.size();
        for(int m = 0; m < ms; m++) {
          final FTMatch ftm = matches.get(m);
          matches.add(new FTMatch().add(ftm).set(ftm.size() - 1, include));
        }
      }
    }

    // collect matches
    match.reset();
    for(final FTMatch ftm : matches) {
      ftm.sort();
      final Iterator<FTStringMatch> iter = ftm.iterator();
      final FTStringMatch first = iter.next();
      for(int end = first.end; end != -1;) {
        if(iter.hasNext()) {
          final FTStringMatch sm = iter.next();
          final int d = pos(sm.start, lexer) - pos(end, lexer) - 1;
          end = d < mn || d > mx ? -1 : sm.end;
        } else {
          match.add(new FTStringMatch(first.start, end, first.pos));
          break;
        }
      }
    }
    return !match.add(excludes).isEmpty();
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
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, DISTANCE, min + "-" + max + ' ' + unit), exprs);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(exprs[0]).token(DISTANCE).paren(min + "-" + max + ' ' + unit);
  }
}
