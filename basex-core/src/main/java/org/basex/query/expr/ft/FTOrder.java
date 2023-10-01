package org.basex.query.expr.ft;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.util.ft.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTOrder expression.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FTOrder extends FTFilter {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr expression
   */
  public FTOrder(final InputInfo info, final FTExpr expr) {
    super(info, expr);
  }

  @Override
  protected boolean filter(final QueryContext qc, final FTMatch match, final FTLexer lexer) {
    int pos = -1, start = -1;
    boolean ordered = false;
    for(final FTStringMatch sm : match) {
      if(sm.exclude) continue;
      if(pos < sm.pos) {
        if(pos > -1 && !ordered) break;
        pos = sm.pos;
        ordered = false;
      }
      if(start < sm.start) {
        start = sm.start;
        ordered = true;
      }
    }
    return ordered;
  }

  @Override
  public FTExpr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new FTOrder(info, exprs[0].copy(cc, vm)));
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, QueryText.ORDERED, TRUE), exprs);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(exprs[0]).token(ORDERED);
  }
}
