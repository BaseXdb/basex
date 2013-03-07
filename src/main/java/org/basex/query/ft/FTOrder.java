package org.basex.query.ft;

import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTOrder expression.
 *
 * @author BaseX Team 2005-12, BSD License
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
  protected boolean filter(final QueryContext ctx, final FTMatch mtc,
      final FTLexer lex) {

    int p = 0, s = 0;
    boolean f = true;
    for(final FTStringMatch sm : mtc) {
      if(sm.ex) continue;
      if(f) {
        if(p == sm.q) continue;
        p = sm.q;
      }
      f = s <= sm.s;
      if(f) s = sm.s;
    }
    return f;
  }

  @Override
  public FTExpr copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
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

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return expr[0].accept(visitor);
  }
}
