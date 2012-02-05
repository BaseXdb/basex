package org.basex.query.ft;

import static org.basex.query.util.Err.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryText;
import org.basex.query.expr.Expr;
import org.basex.query.item.FTNode;
import org.basex.query.iter.FTIter;
import org.basex.query.util.IndexContext;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * FTOptions expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FTWeight extends FTExpr {
  /** Weight. */
  private Expr weight;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param w weight
   */
  public FTWeight(final InputInfo ii, final FTExpr e, final Expr w) {
    super(ii, e);
    weight = w;
  }

  @Override
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    weight = checkUp(weight, ctx).comp(ctx);
    return super.comp(ctx);
  }

  // called by sequential variant
  @Override
  public FTNode item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return weight(expr[0].item(ctx, input), ctx);
  }

  // called by index variant
  @Override
  public FTIter iter(final QueryContext ctx) {
    return new FTIter() {
      @Override
      public FTNode next() throws QueryException {
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
  FTNode weight(final FTNode item, final QueryContext ctx)
      throws QueryException {

    // evaluate weight
    if(item == null) return null;
    final double d = checkDbl(weight, ctx);
    if(Math.abs(d) > 1000) FTWEIGHT.thrw(input, d);
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
  public boolean uses(final Use u) {
    return weight.uses(u) || super.uses(u);
  }

  @Override
  public int count(final Var v) {
    return weight.count(v) + super.count(v);
  }

  @Override
  public boolean removable(final Var v) {
    return weight.removable(v) && super.removable(v);
  }

  @Override
  public FTExpr remove(final Var v) {
    weight = weight.remove(v);
    return super.remove(v);
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
    return expr[0] + " " + QueryText.WEIGHT + ' ' + weight;
  }
}
