package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.SeqType;
import org.basex.query.item.Str;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ItemIter;
import org.basex.query.util.Err;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import static org.basex.util.Token.token;

/**
 * Single order specifier.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class OrderBy extends ParseExpr {
  /** Order expression. */
  private ItemIter seq;
  /** Order expression. */
  private Expr expr;
  /** Ascending/descending order. */
  boolean desc;
  /** Order for empty expressions. */
  boolean lst;

  /**
   * Empty constructor for stable sorting.
   * @param ii input info
   */
  public OrderBy(final InputInfo ii) {
    super(ii);
  }

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param d descending order
   * @param l least empty order
   */
  public OrderBy(final InputInfo ii, final Expr e, final boolean d,
      final boolean l) {
    super(ii);
    seq = new ItemIter();
    expr = e;
    desc = d;
    lst = l;
    type = expr == null ? SeqType.ITEM_ZM : expr.type();
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    if(expr != null) expr = checkUp(expr, ctx).comp(ctx);
    return this;
  }

  /**
   * Adds an item to be sorted.
   * @param ctx query context
   * @throws QueryException query exception
   */
  void add(final QueryContext ctx) throws QueryException {
    if(seq != null) {
      final Iter iter = ctx.iter(expr);
      Item it = iter.next();
      if(it != null) {
        if(iter.next() != null) Err.or(input, XPSORT);
        if(it.node()) it = Str.get(it.atom());
        else if(it.num() && Double.isNaN(it.dbl(input))) it = null;
      }
      seq.add(it);
    }
  }

  /**
   * Resets the built sequence.
   */
  void reset() {
    if(seq != null) seq = new ItemIter();
  }

  /**
   * Returns the specified item.
   * @param i item index
   * @return item
   */
  Item item(final int i) {
    return seq == null ? Itr.get(i) : seq.item[i];
  }

  @Override
  public boolean uses(final Use u) {
    return expr != null && expr.uses(u);
  }

  @Override
  public OrderBy remove(final Var v) {
    if(expr != null) expr = expr.remove(v);
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
    if(expr == null) return "";
    final StringBuilder sb = new StringBuilder(expr.toString());
    if(desc) sb.append(" " + DESCENDING);
    if(!lst) sb.append(" " + EMPTYORD + " " + GREATEST);
    return sb.toString();
  }
}
