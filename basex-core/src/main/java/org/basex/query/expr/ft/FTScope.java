package org.basex.query.expr.ft;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.util.ft.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * FTScope expression.
 *
 * @author BaseX Team 2005-17, BSD License
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
  protected boolean filter(final QueryContext qc, final FTMatch match, final FTLexer lexer) {
    // same unit
    if(same) {
      int s = -1;
      for(final FTStringMatch sm : match) {
        if(sm.exclude) continue;
        final int p = pos(sm.start, lexer);
        if(s == -1) s = p;
        else if(s != p) return false;
        if(sm.start != sm.end && s != pos(sm.end, lexer)) return false;
      }
      return true;
    }
    // different unit
    int c = 0;
    final BoolList bl = new BoolList();
    for(final FTStringMatch sm : match) {
      if(sm.exclude) continue;
      c++;
      final int p = pos(sm.start, lexer), s = bl.size();
      if(p < s && bl.get(p) && p == pos(sm.end, lexer)) return false;
      bl.set(p, true);
    }
    return c > 1;
  }

  @Override
  public FTExpr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new FTScope(info, exprs[0].copy(cc, vm), same, unit);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof FTScope && same == ((FTScope) obj).same &&
        super.equals(obj);
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
