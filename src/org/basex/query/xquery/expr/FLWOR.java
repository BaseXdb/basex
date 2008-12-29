package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Seq;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;

/**
 * FLWOR Clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class FLWOR extends Single {
  /** For/Let expressions. */
  protected ForLet[] fl;
  /** Order Expressions. */
  protected Order order;
  /** Where Expression. */
  protected Expr where;

  /**
   * Constructor.
   * @param f variable inputs
   * @param w where clause
   * @param o order expressions
   * @param r return expression
   */
  public FLWOR(final ForLet[] f, final Expr w, final Order o, final Expr r) {
    super(r);
    fl = f;
    where = w;
    order = o;
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    final int vs = ctx.vars.size();
    
    for(int f = 0; f != fl.length; f++) {
      final Expr e = ctx.comp(fl[f]);
      if(e.e()) {
        ctx.vars.reset(vs);
        ctx.compInfo(OPTSIMPLE, fl[f], e);
        return e;
      }
      fl[f] = (ForLet) e;
    }
    
    if(where != null) {
      where = ctx.comp(where);
      if(where.i()) {
        // test is always false: no results
        if(!((Item) where).bool()) {
          ctx.compInfo(OPTFALSE, where);
          ctx.vars.reset(vs);
          return Seq.EMPTY;
        }
        // always true: test can be skipped
        ctx.compInfo(OPTTRUE, where);
        where = null;
      }
    }

    if(order != null) order.comp(ctx);
    expr = ctx.comp(expr);
    ctx.vars.reset(vs);
    return this;
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

  @Override
  public String color() {
    return "66FF66";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, EVAL, ITER);
    for(final ForLet f : fl) f.plan(ser);
    if(where != null) {
      ser.openElement(WHR);
      where.plan(ser);
      ser.closeElement();
    }
    if(order != null) order.plan(ser);
    ser.openElement(RET);
    expr.plan(ser);
    ser.closeElement();
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(int i = 0; i != fl.length; i++) sb.append((i != 0 ? " " : "") + fl[i]);
    if(where != null) sb.append(" where " + where);
    if(order != null) sb.append(order);
    return sb.append(" return " + expr).toString();
  }
}
