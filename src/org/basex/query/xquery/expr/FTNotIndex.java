package org.basex.query.xquery.expr;

import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.FTNodeItem;
import org.basex.query.xquery.iter.FTNodeIter;

/**
 * FTUnaryNot expression with index access.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class FTNotIndex extends FTExpr {
  /**
   * Constructor.
   * @param e expression
   */
  public FTNotIndex(final FTExpr e) {
    super(e);
  }

  @Override
  public FTNodeIter iter(final XQContext ctx) {
    return new FTNodeIter() {
      @Override
      public FTNodeItem next() throws XQException {
        final FTNodeItem ftni = expr[0].iter(ctx).next();
        ftni.ftn.not = !ftni.ftn.not;
        return ftni;
      }
    };
  }

  @Override
  public String toString() {
    return "ftnotIndex " + expr[0];
  }
}
