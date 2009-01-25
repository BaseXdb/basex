package org.basex.query.ft;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTNodeItem;
import org.basex.query.iter.FTNodeIter;

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
  public FTNodeIter iter(final QueryContext ctx) {
    return new FTNodeIter() {
      @Override
      public FTNodeItem next() throws QueryException {
        final FTNodeItem ftni = expr[0].iter(ctx).next();
        ftni.ftn.not ^= true;
        return ftni;
      }
    };
  }

  @Override
  public String toString() {
    return "ftnotIndex " + expr[0];
  }
}
