package org.basex.query.ft;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTItem;
import org.basex.query.iter.FTIter;

/**
 * FTUnaryNot expression with index access.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Sebastian Gath
 */
final class FTNotIndex extends FTExpr {
  /**
   * Constructor.
   * @param e expression
   */
  FTNotIndex(final FTExpr e) {
    super(e);
  }

  @Override
  public FTIter iter(final QueryContext ctx) throws QueryException {
    return new FTIter() {
      final FTIter ir = expr[0].iter(ctx);

      @Override
      public FTItem next() throws QueryException {
        final FTItem node = ir.next();
        // [CG] FT: check
        //node.fte.not ^= true;
        return node;
      }
    };
  }

  @Override
  public String toString() {
    return "ftnotIndex " + expr[0];
  }
}
