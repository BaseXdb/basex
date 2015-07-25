package org.basex.query.expr.ft;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.util.ft.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTOrder expression.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FTOrder extends FTFilter {
  /**
   * Constructor.
   * @param info input info
   * @param expr expression
   */
  public FTOrder(final InputInfo info, final FTExpr expr) {
    super(info, expr);
  }

  @Override
  protected boolean filter(final QueryContext qc, final FTMatch match, final FTLexer lex) {
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
  public FTExpr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new FTOrder(info, exprs[0].copy(qc, scp, vs));
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(QueryText.ORDERED, TRUE), exprs);
  }

  @Override
  public String toString() {
    return super.toString() + QueryText.ORDERED;
  }
}
