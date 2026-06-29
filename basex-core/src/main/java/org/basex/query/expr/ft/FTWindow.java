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
 * FTWindow expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FTWindow extends FTFilter {
  /** Window. */
  private Expr win;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr expression
   * @param win window
   * @param unit unit
   */
  public FTWindow(final InputInfo info, final FTExpr expr, final Expr win, final FTUnit unit) {
    super(info, expr, unit);
    this.win = win;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(win);
    super.checkUp();
  }

  @Override
  public FTExpr compile(final CompileContext cc) throws QueryException {
    win = win.compile(cc);
    return super.compile(cc).optimize(cc);
  }

  @Override
  public FTExpr optimize(final CompileContext cc) throws QueryException {
    win = win.simplifyFor(Simplify.NUMBER, cc);
    return this;
  }

  @Override
  protected boolean filter(final QueryContext qc, final FTMatch match, final FTLexer lexer)
      throws QueryException {

    final int n = (int) toLong(win, qc) - 1;
    // excluded matches participate in the window offset check; combinations cover includes
    final FTMatch excludes = excludes(match);

    // succeed if any one-occurrence-per-query-position combination fits the window
    for(final FTMatch combo : combine(match)) {
      combo.sort();
      if(window(combo, excludes, n, lexer)) {
        match.reset();
        match.add(combo.get(0));
        return true;
      }
    }
    return false;
  }

  /**
   * Checks whether a combination fits the window (records span/gaps in the first match on
   * success, leaves the shared matches untouched on failure).
   * @param combo include string matches for this combination, sorted by start position
   * @param excludes excluded string matches
   * @param n window size minus one
   * @param lexer tokenizer
   * @return result of check
   */
  private boolean window(final FTMatch combo, final FTMatch excludes, final int n,
      final FTLexer lexer) {

    // accumulate span and gaps without mutating, checking the window size
    final FTStringMatch first = combo.get(0);
    int end = first.end;
    boolean gaps = first.gaps;
    for(final FTStringMatch sm : combo) {
      gaps |= sm.end - end > 1;
      end = sm.end;
      if(pos(end, lexer) - pos(first.start, lexer) > n) return false;
    }

    // find a window offset with no excluded match inside
    final int w = n - pos(end, lexer) + pos(first.start, lexer);
    for(int s = pos(first.start, lexer) - w; s <= pos(first.start, lexer); ++s) {
      boolean h = false;
      for(final FTStringMatch sm : excludes) {
        h = pos(sm.start, lexer) >= s && pos(sm.end, lexer) <= s + w;
        if(h) break;
      }
      if(!h) {
        first.end = end;
        first.gaps = gaps;
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean has(final Flag... flags) {
    return win.has(flags) || super.has(flags);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    return win.inlineable(ic) && super.inlineable(ic);
  }

  @Override
  public VarUsage count(final Var var) {
    return win.count(var).plus(super.count(var));
  }

  @Override
  public FTExpr inline(final InlineContext ic) throws QueryException {
    final boolean changed = ic.inline(exprs);
    final Expr inlined = win.inline(ic);
    if(inlined != null) win = inlined;
    return changed || inlined != null ? optimize(ic.cc) : null;
  }

  @Override
  public FTExpr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new FTWindow(info, exprs[0].copy(cc, vm), win.copy(cc, vm), unit));
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return super.accept(visitor) && win.accept(visitor);
  }

  @Override
  public int exprSize() {
    int size = 1;
    for(final FTExpr expr : exprs) size += expr.exprSize();
    return size + win.exprSize();
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, WINDOW, unit), win, exprs);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(exprs[0]).token(WINDOW).token(win).token(unit);
  }
}
