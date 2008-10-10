package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;

/**
 * FLWOR Clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FLWOR extends FLWR {
  /** Order Expressions. */
  private final Order order;

  /**
   * Constructor.
   * @param f variable inputs
   * @param w where clause
   * @param o order expressions
   * @param r return expression
   */
  public FLWOR(final ForLet[] f, final Expr w, final Order o, final Expr r) {
    super(f, w, r);
    order = o;
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    order.comp(ctx);
    return super.comp(ctx);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final SeqIter seq = new SeqIter();
    final Iter[] iter = new Iter[fl.length];
    for(int f = 0; f < fl.length; f++) iter[f] = fl[f].iter(ctx);
    iter(ctx, seq, iter, 0);
    return order.iter(seq);
  }

  /**
   * Performs a recursive iteration on the specified variable position.
   * @param ctx root reference
   * @param seq result sequence
   * @param it iterator
   * @param p variable position
   * @throws XQException evaluation exception
   */
  void iter(final XQContext ctx, final SeqIter seq,
      final Iter[] it, final int p) throws XQException {

    final boolean more = p + 1 != fl.length;
    while(it[p].next().bool()) {
      if(more) {
        iter(ctx, seq, it, p + 1);
      } else {
        if(where == null || ctx.iter(where).ebv().bool()) {
          order.add(ctx);
          seq.add(ctx.iter(expr).finish());
        }
      }
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(int i = 0; i != fl.length; i++) sb.append((i != 0 ? " " : "") + fl[i]);
    if(where != null) sb.append(" where " + where);
    sb.append(order);
    return sb.append(" return " + expr).toString();
  }

  @Override
  public String info() {
    return "FLWOR expression";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    for(final ForLet f : fl) f.plan(ser);
    if(where != null) where.plan(ser);
    order.plan(ser);
    ser.openElement(RET);
    expr.plan(ser);
    ser.closeElement();
  }
}
