package org.basex.query.xquery.expr;

import org.basex.query.xquery.IndexContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.iter.Iter;

/**
 * FTUnaryNot expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
  public Iter iter(final XQContext ctx) throws XQException {
    return Dbl.get(ctx.iter(expr[0]).next().bool() ? 0 : 1).iter();
  }

  @Override
  public String toString() {
    return "ftnot " + expr[0];
  }
  
  @Override
  public void indexAccessible(final XQContext ctx, final IndexContext ic) 
    throws XQException {
    
    ic.ftnot ^= true;
    // in case of ftand ftnot seq could be set false in FTAnd
    ic.seq = true;
    expr[0].indexAccessible(ctx, ic);
    ic.is = Integer.MAX_VALUE;
  }

  @Override
  public Expr indexEquivalent(final XQContext ctx, final IndexContext ic)
    throws XQException {
    return new FTNotIndex((FTExpr) expr[0].indexEquivalent(ctx, ic));
  }
}
