package org.basex.query.xquery.expr;

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
    return Dbl.iter(ctx.iter(expr[0]).next().bool() ? 0 : 1);
  }

  @Override
  public String toString() {
    return "ftnot " + expr[0];
  }
}
