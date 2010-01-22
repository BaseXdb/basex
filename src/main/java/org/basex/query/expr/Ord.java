package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.Str;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Err;
import org.basex.query.util.Var;

/**
 * Single order specifier.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Ord extends Expr {
  /** Order expression. */
  private SeqIter seq;
  /** Order expression. */
  private Expr expr;
  /** Ascending/descending order. */
  boolean desc;
  /** Order for empty expressions. */
  boolean lst;

  /**
   * Empty constructor for stable sorting.
   */
  public Ord() { }

  /**
   * Constructor.
   * @param e expression
   * @param d descending order
   * @param l least empty order
   */
  public Ord(final Expr e, final boolean d, final boolean l) {
    seq = new SeqIter();
    expr = e;
    desc = d;
    lst = l;
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
        if(iter.next() != null) Err.or(XPSORT);
        if(it.node()) it = Str.get(it.str());
        else if(it.n() && Double.isNaN(it.dbl())) it = null;
      }
      seq.add(it);
    }
  }

  /**
   * Resets the built sequence.
   */
  void finish() {
    if(seq != null) seq = new SeqIter();
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
  public boolean uses(final Use u, final QueryContext ctx) {
    return expr != null && expr.uses(u, ctx);
  }

  @Override
  public Ord remove(final Var v) {
    if(expr != null) expr = expr.remove(v);
    return this;
  }

  @Override
  public Return returned(final QueryContext ctx) {
    return expr == null ? Return.SEQ : expr.returned(ctx);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    expr.plan(ser);
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
