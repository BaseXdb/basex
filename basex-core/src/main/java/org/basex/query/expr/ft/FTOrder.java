package org.basex.query.expr.ft;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.util.ft.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTOrder expression.
 *
 * @author BaseX Team 2005-21, BSD License
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
  protected boolean filter(final QueryContext qc, final FTMatch match, final FTLexer lexer) {
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
  public FTExpr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new FTOrder(info, exprs[0].copy(cc, vm)));
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this, QueryText.ORDERED, TRUE), exprs);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(exprs[0]).token(ORDERED);
  }
}
