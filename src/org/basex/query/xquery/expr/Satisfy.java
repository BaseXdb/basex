package org.basex.query.xquery.expr;

import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.iter.Iter;

/**
 * Some/Every Satisfier Clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Satisfy extends Single {
  /** For/Let expressions. */
  private final ForLet[] fl;
  /** Every flag. */
  private final boolean every;

  /**
   * Constructor.
   * @param f variable inputs
   * @param s satisfier
   * @param e every flag
   */
  public Satisfy(final For[] f, final Expr s, final boolean e) {
    super(s);
    fl = f;
    every = e;
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    for(int f = 0; f != fl.length; f++) {
      final Expr e = fl[f].comp(ctx);
      if(e.e()) return Bln.get(every);
      fl[f] = (ForLet) e;
    }
    return super.comp(ctx);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Iter[] iter = new Iter[fl.length];
    for(int f = 0; f < fl.length; f++) iter[f] = fl[f].iter(ctx);
    return Bln.get(iter(ctx, iter, 0)).iter();
  }

  /**
   * Performs a recursive iteration on the specified variable position.
   * @param ctx root reference
   * @param it iterator
   * @param p variable position
   * @return satisfied flag
   * @throws XQException evaluation exception
   */
  private boolean iter(final XQContext ctx, final Iter[] it, final int p)
    throws XQException {

    final boolean last = p + 1 == fl.length;
    while(it[p].next().bool()) {
      final boolean res = last ? ctx.iter(expr).ebv().bool() :
        iter(ctx, it, p + 1);
      if(res ^ every) {
        for(int l = 0; l < fl.length; l++) it[l].reset();
        return res;
      }
    }
    return every;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(every ? "every " : "some ");
    for(int i = 0; i < fl.length; i++) {
      if(i != 0) sb.append(", ");
      sb.append(fl[i]);
    }
    return sb.append(" satisfies " + expr).toString();
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this);
    for(ForLet f : fl) f.plan(ser);
    expr.plan(ser);
    ser.closeElement(this);
  }
}
