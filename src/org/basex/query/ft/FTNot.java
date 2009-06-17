package org.basex.query.ft;

import static org.basex.query.QueryTokens.*;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTItem;

/**
 * FTUnaryNot expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTNot extends FTExpr {
  /**
   * Constructor.
   * @param e expression
   */
  public FTNot(final FTExpr e) {
    super(e);
  }

  @Override
  public FTItem atomic(final QueryContext ctx) throws QueryException {
    final FTItem it = expr[0].atomic(ctx);
    it.all.not();
    // needed to support negated queries without hits ('a' ftcontains ftnot 'b')
    it.score(it.score() == 0 ? 1 : 0);
    return it;
  }

  @Override
  public boolean usesExclude() {
    return true;
  }

  @Override
  public String toString() {
    return FTNOT + " " + expr[0];
  }



  // [CG] FT: to be revised...

  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    // [CG] FT: skip index access
    if(1 == 1) return false;

    ic.ftnot ^= true;
    // in case of ftand ftnot seq could be set false in FTAnd
    ic.seq = true;
    final boolean ia = expr[0].indexAccessible(ic);
    ic.is = Integer.MAX_VALUE;
    return ia;
  }

  @Override
  public FTExpr indexEquivalent(final IndexContext ic) throws QueryException {
    return new FTNotIndex(expr[0].indexEquivalent(ic));
  }
}
