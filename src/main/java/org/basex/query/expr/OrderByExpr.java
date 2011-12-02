package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import static org.basex.util.Token.token;

/**
 * Single order specifier.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class OrderByExpr extends OrderBy {
  /** Order expression. */
  private Expr expr;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param d descending order
   * @param l least empty order
   */
  public OrderByExpr(final InputInfo ii, final Expr e, final boolean d,
      final boolean l) {
    super(ii);
    expr = e;
    desc = d;
    lst = l;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    expr = checkUp(expr, ctx).comp(ctx);
    type = expr.type();
    return this;
  }

  @Override
  Item key(final QueryContext ctx, final int i) throws QueryException {
    Item it = expr.item(ctx, input);
    if(it != null) {
      if(it.type.isNode()) it = Str.get(it.string(input));
      else if(it.type.isNumber() && Double.isNaN(it.dbl(input))) it = null;
    }
    return it;
  }

  @Override
  public boolean uses(final Use u) {
    return expr.uses(u);
  }

  @Override
  public int count(final Var v) {
    return expr.count(v);
  }

  @Override
  public boolean removable(final Var v) {
    return expr.removable(v);
  }

  @Override
  public OrderByExpr remove(final Var v) {
    expr = expr.remove(v);
    return this;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, DIR, token(desc ? DESCENDING : ASCENDING),
        token(EMPTYORD), token(lst ? LEAST : GREATEST));
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(expr.toString());
    if(desc) sb.append(" " + DESCENDING);
    if(!lst) sb.append(" " + EMPTYORD + " " + GREATEST);
    return sb.toString();
  }
}
