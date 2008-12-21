package org.basex.query.xquery.expr;

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
  public Iter iter(final XQContext ctx) throws XQException {
    final SeqIter seq = new SeqIter();
    final Iter[] iter = new Iter[fl.length];
    for(int f = 0; f < fl.length; f++) iter[f] = ctx.iter(fl[f]);
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
  private void iter(final XQContext ctx, final SeqIter seq,
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
}
