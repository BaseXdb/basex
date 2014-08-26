package org.basex.query.expr.ft;

import static org.basex.query.QueryText.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * FTScope expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FTScope extends FTFilter {
  /** Same/different flag. */
  private final boolean same;

  /**
   * Constructor.
   * @param info input info
   * @param expr expression
   * @param same same flag
   * @param unit unit
   */
  public FTScope(final InputInfo info, final FTExpr expr, final boolean same, final FTUnit unit) {
    super(info, expr, unit);
    this.same = same;
  }

  @Override
  protected boolean filter(final QueryContext qc, final FTMatch mtc, final FTLexer lex) {
    if(same) {
      int s = -1;
      for(final FTStringMatch sm : mtc) {
        if(sm.exclude) continue;
        final int p = pos(sm.start, lex);
        if(s == -1) s = p;
        else if(s != p) return false;
      }
      return true;
    }
    int c = 0;
    final BoolList bl = new BoolList();
    for(final FTStringMatch sm : mtc) {
      if(sm.exclude) continue;
      c++;
      final int p = pos(sm.start, lex);
      final int s = bl.size();
      if(p < s && bl.get(p) && p == pos(sm.end, lex)) return false;
      bl.set(p, true);
    }
    return c > 1;
  }

  @Override
  public FTExpr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new FTScope(info, exprs[0].copy(qc, scp, vs), same, unit);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(same ? SAME : DIFFERENT, unit), exprs);
  }

  @Override
  public String toString() {
    return super.toString() + (same ? SAME : DIFFERENT) + ' ' + unit;
  }
}
