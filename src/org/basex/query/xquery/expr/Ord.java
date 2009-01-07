package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Itr;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;
import org.basex.query.xquery.util.Err;

/**
 * Single order specifier.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Ord {
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
  
  /**
   * Compiles the expression to be sorted.
   * @param ctx query context
   * @throws XQException query exception
   */
  public void comp(final XQContext ctx) throws XQException {
    if(expr != null) expr = expr.comp(ctx);
  }

  /**
   * Adds an item to be sorted.
   * @param ctx query context
   * @throws XQException query exception
   */
  public void add(final XQContext ctx) throws XQException {
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
  public void finish() {
    if(seq != null) seq = new SeqIter();
  }

  /**
   * Returns the specified item.
   * @param i item index
   * @return item
   */
  public Item item(final int i) {
    return seq == null ? Itr.get(i) : seq.item[i];
  }
  
  @Override
  public String toString() {
    if(expr == null) return "";
    final StringBuilder sb = new StringBuilder(expr.toString());
    if(desc) sb.append(" ascending");
    if(!lst) sb.append(" empty greatest");
    return sb.toString();
  }

  /**
   * Serializes the abstract syntax tree.
   * @param ser serializer
   * @throws IOException exception
   */
  public void plan(final Serializer ser) throws IOException {
    expr.plan(ser);
  }
}
