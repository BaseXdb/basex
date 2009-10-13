package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Var;

/**
 * FLWOR clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class FLWOR extends Expr {
  /** Expression list. */
  protected Expr ret;
  /** For/Let expressions. */
  protected ForLet[] fl;
  /** Where Expression. */
  protected Expr where;
  /** Order Expressions. */
  protected Order order;

  /** Group by Expression. */
  protected Group group;
  /**
   * Constructor.
   * @param f variable inputs
   * @param w where clause
   * @param o order expressions
   * @param r return expression
   */
  public FLWOR(final ForLet[] f, final Expr w, final Order o, final Expr r) {
    ret = r;
    fl = f;
    group = null;
    where = w;
    order = o;
  }

  /** FLWOR initialization.
   * @param f variable inputs
   * @param w where clause
   * @param o order expression
   * @param g group by expression
   * @param r return expression
   */
  public FLWOR(final ForLet[] f, final Expr w, final Order o, final Group g,
      final Expr r) {
    ret = r;
    fl = f;
    where = w;
    group = g;
    order = o;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    final int vs = ctx.vars.size();

    for(int f = 0; f != fl.length; f++) {
      // disable fast ftcontains evaluation if score value exists
      final boolean fast = ctx.ftfast;
      ctx.ftfast &= fl[f].standard();
      final Expr e = fl[f].comp(ctx);
      ctx.ftfast = fast;

      if(e.e()) {
        ctx.vars.reset(vs);
        ctx.compInfo(OPTFLWOR);
        return e;
      }
      fl[f] = (ForLet) e;
    }

    if(where != null) {
      where = where.comp(ctx);
      final boolean e = where.e();
      if(e || where.i()) {
        // test is always false: no results
        if(e || !((Item) where).bool()) {
          ctx.compInfo(OPTFALSE, where);
          ctx.vars.reset(vs);
          return Seq.EMPTY;
        }
        // always true: test can be skipped
        ctx.compInfo(OPTTRUE, where);
        where = null;
      }
    }
    if(group != null) group.comp(ctx);
    if(order != null) order.comp(ctx);
    ret = ret.comp(ctx);

    ctx.vars.reset(vs);
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final SeqIter seq = new SeqIter();
    final Iter[] iter = new Iter[fl.length];
    for(int f = 0; f < fl.length; f++) iter[f] = ctx.iter(fl[f]);
    iter(ctx, seq, iter, 0);

    if(order != null) {
      order.sq = seq;
      final Item m = order.iter(ctx).finish();
      if(group == null) return m.iter(); // return now
      group.sq = m != null ? (SeqIter) m.iter() : seq;
    } else group.sq = seq;
    return group.iter(ctx);
  }

  /**
   * Performs a recursive iteration on the specified variable position.
   * @param ctx root reference
   * @param seq result sequence
   * @param it iterator
   * @param p variable position
   * @throws QueryException query exception
   */
  private void iter(final QueryContext ctx, final SeqIter seq,
      final Iter[] it, final int p) throws QueryException {

    final boolean more = p + 1 != fl.length;
    while(it[p].next().bool()) {
      if(more) {
        iter(ctx, seq, it, p + 1);
      } else {
        if(where == null || where.ebv(ctx).bool()) {
          if(group != null) group.add(ctx);
          if(order != null) order.add(ctx);
          seq.add(ctx.iter(ret).finish());
        }
      }
    }
  }

  @Override
  public final boolean uses(final Use u, final QueryContext ctx) {
    return u == Use.VAR || ret.uses(u, ctx);
  }

  @Override
  public final boolean removable(final Var v, final QueryContext ctx) {
    for(final ForLet f : fl) {
      if(!f.removable(v, ctx)) return false;
      if(f.shadows(v)) return true;
    }
    return (where == null || where.removable(v, ctx)) &&
      (order == null || order.removable(v, ctx)) && ret.removable(v, ctx);
  }

  @Override
  public final Expr remove(final Var v) {
    for(final ForLet f : fl) {
      f.remove(v);
      if(f.shadows(v)) return this;
    }
    if(where != null) where = where.remove(v);
    if(order != null) order = order.remove(v);
    ret = ret.remove(v);
    return this;
  }

  @Override
  public final Return returned(final QueryContext ctx) {
    return Return.SEQ;
  }

  @Override
  public final String color() {
    return "66FF66";
  }

  @Override
  public final void plan(final Serializer ser) throws IOException {
    ser.openElement(this, EVAL, ITER);
    for(final ForLet f : fl) f.plan(ser);
    if(where != null) {
      ser.openElement(WHR);
      where.plan(ser);
      ser.closeElement();
    }
    if(group != null) group.plan(ser);
    if(order != null) order.plan(ser);
    ser.openElement(RET);
    ret.plan(ser);
    ser.closeElement();
    ser.closeElement();
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    for(int i = 0; i != fl.length; i++) sb.append((i != 0 ? " " : "") + fl[i]);
    if(where != null) sb.append(" " + WHERE + " " + where);
    if(order != null) sb.append(order);
    if(group != null) sb.append(group);
    return sb.append(" " + RETURN + " " + ret).toString();
  }
}
