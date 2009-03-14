package org.basex.query.ft;

import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.iter.FTNodeIter;

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
  public FTNodeIter iter(final QueryContext ctx) throws QueryException {
    // do not color fulltext if there is a ftnot
    ctx.ftd = null;
    return score(expr[0].iter(ctx).next().score() == 0 ? 1 : 0);
  }

  @Override
  public String toString() {
    return "ftnot " + expr[0];
  }
  
  @Override
  public void indexAccessible(final QueryContext ctx, final IndexContext ic) 
    throws QueryException {
    
    ic.ftnot ^= true;
    // in case of ftand ftnot seq could be set false in FTAnd
    ic.seq = true;
    expr[0].indexAccessible(ctx, ic);
    ic.is = Integer.MAX_VALUE;
  }

  @Override
  public FTExpr indexEquivalent(final QueryContext ctx, final IndexContext ic)
    throws QueryException {
    return new FTNotIndex(expr[0].indexEquivalent(ctx, ic));
  }

  @Override
  public boolean usesExclude() {
    return true;
  }
}
