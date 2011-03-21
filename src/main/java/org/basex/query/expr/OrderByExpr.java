package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.iter.ItemCache;
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
  /** Sequence to be ordered. */
  private ItemCache seq;
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
  void init(final int s) {
    if(seq == null) seq = s > 0 ? new ItemCache(s) : new ItemCache();
    else seq.size(0);
  }

  @Override
  void add(final QueryContext ctx) throws QueryException {
    Item it = expr.item(ctx, input);
    if(it != null) {
      if(it.node()) it = Str.get(it.atom(input));
      else if(it.num() && Double.isNaN(it.dbl(input))) it = null;
    }
    seq.add(it);
  }

  @Override
  Item get(final int i) {
    return seq.get(i);
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
