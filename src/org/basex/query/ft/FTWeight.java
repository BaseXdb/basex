package org.basex.query.ft;

import static org.basex.query.QueryText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryTokens;
import org.basex.query.expr.Expr;
import org.basex.query.item.FTItem;
import org.basex.query.iter.FTIter;
import org.basex.query.util.Err;

/**
 * FTOptions expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTWeight extends FTExpr {
  /** Weight. */
  private Expr weight;

  /**
   * Constructor.
   * @param e expression
   * @param w weight
   */
  public FTWeight(final FTExpr e, final Expr w) {
    super(e);
    weight = w;
  }

  @Override
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    weight = weight.comp(ctx);
    return super.comp(ctx);
  }

  // called by sequential variant
  @Override
  public FTItem atomic(final QueryContext ctx) throws QueryException {
    return weight(expr[0].atomic(ctx), ctx);
  }

  // called by index variant
  @Override
  public FTIter iter(final QueryContext ctx) {
    return new FTIter() {
      @Override
      public FTItem next() throws QueryException {
        return weight(expr[0].iter(ctx).next(), ctx);
      }
    };
  }

  /**
   * Returns the item with weight calculation.
   * @param item input item
   * @param ctx query context
   * @return item
   * @throws QueryException query exception
   */
  FTItem weight(final FTItem item, final QueryContext ctx)
      throws QueryException {

    // evaluate weight
    if(item == null) return null;
    final double d = checkDbl(weight, ctx);
    if(Math.abs(d) > 1000) Err.or(FTWEIGHT, d);
    if(d == 0) item.all.size = 0;
    item.score(item.score() * d);
    return item;
  }
  
  @Override
  public boolean indexAccessible(final IndexContext ic) {
    // weight makes no sense as long as no index-based scoring exists
    return false;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    weight.plan(ser);
    expr[0].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return expr[0] + " " + QueryTokens.WEIGHT + " " + weight;
  }
}
