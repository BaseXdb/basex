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
 * @author BaseX Team, BSD License
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
    return select(match, FTOrder::ordered, FTOrder::ordered);
  }

  /**
   * Checks if the string matches of a combination are ordered by their query positions.
   * @param combo include string matches, ordered by query position
   * @return result of check
   */
  private static boolean ordered(final FTMatch combo) {
    int start = -1;
    for(final FTStringMatch sm : combo) {
      if(sm.start < start) return false;
      start = sm.start;
    }
    return true;
  }

  /**
   * Checks if an exclude is ordered relative to all includes of a combination.
   * @param ex exclude
   * @param combo include string matches
   * @return result of check
   */
  private static boolean ordered(final FTStringMatch ex, final FTMatch combo) {
    for(final FTStringMatch sm : combo) {
      if(!(ex.start <= sm.start && ex.pos <= sm.pos || ex.start >= sm.start && ex.pos >= sm.pos)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public FTExpr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new FTOrder(info, exprs[0].copy(cc, vm)));
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, ORDERED, TRUE), exprs);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(exprs[0]).token(ORDERED);
  }
}
