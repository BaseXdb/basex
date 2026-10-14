package org.basex.query.expr.ft;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.ft.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTDistance expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FTDistance extends FTFilter {
  /** Minimum distance. */
  private Expr min;
  /** Maximum distance. */
  private Expr max;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
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
    final FTMatch excludes = excludes(match);
    final ArrayList<FTMatch> matches = combine(match);

    // collect matches, and excludes within the distance range of an include
    match.reset();
    if(matches.isEmpty()) return true;
    final FTMatch kept = new FTMatch();
    for(final FTMatch ftm : matches) {
      ftm.sort();
      final Iterator<FTStringMatch> iter = ftm.iterator();
      final FTStringMatch first = iter.next();
      int last = first.end, end = last;
      boolean gaps = first.gaps, ok = true;
      while(ok && iter.hasNext()) {
        final FTStringMatch sm = iter.next();
        ok = distance(sm, last, mn, mx, lexer);
        gaps |= sm.gaps || sm.start > end + 1;
        last = sm.end;
        end = Math.max(end, last);
      }
      if(ok) {
        final FTStringMatch joined = new FTStringMatch(first.start, end, first.pos);
        joined.gaps = gaps;
        match.add(joined);
        for(final FTStringMatch ex : excludes) {
          if(!kept.contains(ex) && in(ex, ftm, mn, mx, lexer)) kept.add(ex);
        }
      }
    }
    return !match.add(kept).isEmpty();
  }

  /**
   * Checks if the distance between a string match and a preceding end position is in range.
   * @param sm string match
   * @param end end position of the preceding string match
   * @param mn minimum distance
   * @param mx maximum distance
   * @param lexer tokenizer
   * @return result of check
   */
  private boolean distance(final FTStringMatch sm, final int end, final long mn, final long mx,
      final FTLexer lexer) {
    final int d = pos(sm.start, lexer) - pos(end, lexer) - 1;
    return d >= mn && d <= mx;
  }

  /**
   * Checks if an exclude is within the distance range of one of the includes.
   * @param ex exclude
   * @param includes includes
   * @param mn minimum distance
   * @param mx maximum distance
   * @param lexer tokenizer
   * @return result of check
   */
  private boolean in(final FTStringMatch ex, final FTMatch includes, final long mn,
      final long mx, final FTLexer lexer) {
    for(final FTStringMatch sm : includes) {
      if(sm.compareTo(ex) <= 0 ? distance(ex, sm.end, mn, mx, lexer) :
        distance(sm, ex.end, mn, mx, lexer)) return true;
    }
    return false;
  }

  @Override
  public boolean has(final Flag... flags) {
    return min.has(flags) || max.has(flags) || super.has(flags);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    return min.inlineable(ic) && max.inlineable(ic) && super.inlineable(ic);
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
  public FTExpr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new FTDistance(info, exprs[0].copy(cc, vm), min.copy(cc, vm), max.copy(cc, vm),
        unit));
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return super.accept(visitor) && visitAll(visitor, min, max);
  }

  @Override
  public int exprSize() {
    return super.exprSize() + min.exprSize() + max.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final FTDistance ftd && min.equals(ftd.min) &&
        max.equals(ftd.max) && super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, DISTANCE, min + "-" + max + ' ' + unit), exprs);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(exprs[0]).token(DISTANCE);
    range(qs, min, max, Itr.MIN);
    qs.token(unit);
  }
}
