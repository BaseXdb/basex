package org.basex.query.ft;

import static org.basex.query.QueryText.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.list.*;

/**
 * FTScope expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FTScope extends FTFilter {
  /** Same/different flag. */
  private final boolean same;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param u unit
   * @param s same flag
   */
  public FTScope(final InputInfo ii, final FTExpr e, final FTUnit u,
      final boolean s) {
    super(ii, e);
    unit = u;
    same = s;
  }

  @Override
  protected boolean filter(final QueryContext ctx, final FTMatch mtc,
      final FTLexer lex) {

    if(same) {
      int s = -1;
      for(final FTStringMatch sm : mtc) {
        if(sm.ex) continue;
        final int p = pos(sm.s, lex);
        if(s == -1) s = p;
        else if(s != p) return false;
      }
      return true;
    }
    int c = 0;
    final BoolList bl = new BoolList();
    for(final FTStringMatch sm : mtc) {
      if(sm.ex) continue;
      c++;
      final int p = pos(sm.s, lex);
      final int s = bl.size();
      if(p < s && bl.get(p) && p == pos(sm.e, lex)) return false;
      bl.set(p, true);
    }
    return c > 1;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(same ? SAME : DIFFERENT, unit), expr);
  }

  @Override
  public String toString() {
    return super.toString() + (same ? SAME : DIFFERENT) + ' ' + unit;
  }

  @Override
  public boolean visitVars(final VarVisitor visitor) {
    return visitor.visitAll(expr);
  }
}
