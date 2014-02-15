package org.basex.query.ft;

import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTOrder expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FTOrder extends FTFilter {
  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   */
  public FTOrder(final InputInfo ii, final FTExpr e) {
    super(ii, e);
  }

  @Override
  protected boolean filter(final QueryContext ctx, final FTMatch match, final FTLexer lex) {
    int pos = 0, start = 0;
    for(final FTStringMatch sm : match) {
      if(sm.exclude || pos == sm.pos) continue;
      if(start > sm.start) return false;
      pos = sm.pos;
      start = sm.start;
    }
    return true;
  }

  @Override
  public FTExpr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new FTOrder(info, expr[0].copy(ctx, scp, vs));
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(QueryText.ORDERED, TRUE), expr);
  }

  @Override
  public String toString() {
    return super.toString() + QueryText.ORDERED;
  }
}
