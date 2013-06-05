package org.basex.query.ft;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTContent expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FTContent extends FTFilter {
  /** Start flag. */
  private final boolean start;
  /** End flag. */
  private final boolean end;

  /**
   * Constructor.
   * @param ii input info
   * @param ex expression
   * @param s start flag
   * @param e end flag
   */
  public FTContent(final InputInfo ii, final FTExpr ex, final boolean s,
      final boolean e) {
    super(ii, ex);
    start = s;
    end = e;
  }

  @Override
  protected boolean filter(final QueryContext ctx, final FTMatch mtc,
      final FTLexer lex) {
    if(start) {
      for(final FTStringMatch sm : mtc) if(sm.s == 0) return true;
    } else if(end) {
      final int p = lex.count() - 1;
      for(final FTStringMatch sm : mtc) if(sm.e == p) return true;
    } else {
      final int s = lex.count();
      final boolean[] bl = new boolean[s];
      for(final FTStringMatch sm : mtc) {
        if(sm.g) continue;
        for(int p = sm.s; p <= sm.e; ++p) bl[p] = true;
      }
      for(final boolean b : bl) if(!b) return false;
      return true;
    }
    return false;
  }

  @Override
  protected boolean content() {
    return end || !start;
  }

  @Override
  public FTExpr copy(final QueryContext ctx, final VarScope scp,
      final IntObjMap<Var> vs) {
    return new FTContent(info, expr[0].copy(ctx, scp, vs), start, end);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(start ? START : end ? END : CONTENT, TRUE), expr);
  }

  @Override
  public String toString() {
    return super.toString() + (start || end ? AT + ' ' +
        (start ? START : END) : ENTIRE + ' ' + CONTENT);
  }
}
