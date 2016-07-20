package org.basex.query.expr.ft;

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
 * FTWindow expression.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class FTWindow extends FTFilter {
  /** Window. */
  private Expr win;

  /**
   * Constructor.
   * @param info input info
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
    return super.compile(cc);
  }

  @Override
  protected boolean filter(final QueryContext qc, final FTMatch mtc, final FTLexer lex)
      throws QueryException {

    final int n = (int) toLong(win, qc) - 1;
    mtc.sort();

    FTStringMatch f = null;
    for(final FTStringMatch m : mtc) {
      if(m.exclude) continue;
      if(f == null) f = m;
      f.gaps |= m.end - f.end > 1;
      f.end = m.end;
      if(pos(f.end, lex) - pos(f.start, lex) > n) return false;
    }
    if(f == null) return false;

    final int w = n - pos(f.end, lex) + pos(f.start, lex);
    for(int s = pos(f.start, lex) - w; s <= pos(f.start, lex); ++s) {
      boolean h = false;
      for(final FTStringMatch m : mtc) {
        h = m.exclude && pos(m.start, lex) >= s && pos(m.end, lex) <= s + w;
        if(h) break;
      }
      if(!h) {
        mtc.reset();
        mtc.add(f);
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean has(final Flag flag) {
    return win.has(flag) || super.has(flag);
  }

  @Override
  public boolean removable(final Var var) {
    return win.removable(var) && super.removable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    return win.count(var).plus(super.count(var));
  }

  @Override
  public FTExpr inline(final Var var, final Expr ex, final CompileContext cc)
      throws QueryException {
    final boolean e = inlineAll(exprs, var, ex, cc);
    final Expr w = win.inline(var, ex, cc);
    if(w != null) win = w;
    return e || w != null ? optimize(cc) : null;
  }

  @Override
  public FTExpr copy(final CompileContext cc, final IntObjMap<Var> vs) {
    return new FTWindow(info, exprs[0].copy(cc, vs), win.copy(cc, vs), unit);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(QueryText.WINDOW, unit), win, exprs);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return super.accept(visitor) && win.accept(visitor);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final FTExpr expr : exprs) sz += expr.exprSize();
    return sz + win.exprSize();
  }

  @Override
  public String toString() {
    return super.toString() + QueryText.WINDOW + ' ' + win + ' ' + unit;
  }
}
