package org.basex.query.ft;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTNode;
import org.basex.query.iter.FTNodeIter;

/**
 * FTUnaryNot expression with index access.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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
  public FTNodeIter iter(final QueryContext ctx) throws QueryException {
    return new FTNodeIter() {
      final FTNodeIter ir = expr[0].iter(ctx);

      @Override
      public FTNode next() throws QueryException {
        final FTNode node = ir.next();
        node.fte.not ^= true;
        return node;
      }
    };
  }

  @Override
  public String toString() {
    return "ftnotIndex " + expr[0];
  }
}
