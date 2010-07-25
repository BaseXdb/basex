package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import java.util.HashMap;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.GroupPartition.GroupNode;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.ItemList;
import org.basex.query.util.Var;

/**
 * GFLWOR clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class GFLWOR extends Expr {
  /** Expression list. */
  private Expr ret;
  /** For/Let expressions. */
  private final ForLet[] fl;
  /** Where expression. */
  private Expr where;
  /** Order expressions. */
  private Order order;
  /** Group by expression. */
  private final Group group;

  /**
   * GFLWOR initialization.
   * @param f variable inputs
   * @param w where clause
   * @param o order expression
   * @param g group by expression
   * @param r return expression
   */
  public GFLWOR(final ForLet[] f, final Expr w, final Order o, final Group g,
      final Expr r) {
    ret = r;
    fl = f;
    where = w;
    group = g;
    order = o;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    checkUp(where, ctx);

    boolean grp = ctx.grouping;
    ctx.grouping = group != null;
    final int vs = ctx.vars.size();

    for(int f = 0; f != fl.length; f++) {
      // disable fast full-text evaluation if score value exists
      final boolean fast = ctx.ftfast;
      ctx.ftfast = ctx.ftfast && fl[f].standard();
      fl[f] = fl[f].comp(ctx);
      ctx.ftfast = fast;
    }

    boolean em = false;
    if(where != null) {
      where = checkUp(where, ctx).comp(ctx);
      em = where.e();
      if(!em && where.i()) {
        // test is always false: no results
        em = !((Item) where).bool();
        if(!em) {
          // always true: test can be skipped
          ctx.compInfo(OPTTRUE, where);
          where = null;
        }
      }
    }

    if(group != null) group.comp(ctx);
    if(order != null) order.comp(ctx);
    ret = ret.comp(ctx);

    ctx.vars.reset(vs);
    ctx.grouping = grp;

    if(em) {
      ctx.compInfo(OPTFALSE, where);
      return Seq.EMPTY;
    }

    for(int f = 0; f != fl.length; f++) {
      // remove GFLWOR clause if it the FOR clause is empty
      if(fl[f].expr.e() && fl[f] instanceof For) {
        ctx.compInfo(OPTFLWOR);
        return Seq.EMPTY;
      }
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final HashMap<Var, ItemList> cache =
      new HashMap<Var, ItemList>();

    final Iter[] iter = new Iter[fl.length];

    // store pointers to the cached results here.
    for(int f = 0; f < fl.length; f++) { // fill each var with data
      iter[f] = ctx.iter(fl[f]);
      if(group != null) cache.put(fl[f].var, new ItemList());
    }

    // evaluate pre grouping tuples
    group.initgroup(fl);
    iter(ctx, cache, iter, 0);

    final int vs = ctx.vars.size();
    for(ForLet aFl : fl) ctx.vars.add(aFl.var);

    final SeqIter si = new SeqIter();
    retG(ctx, si);
    ctx.vars.reset(vs);
    return si;

    // Move variables to stack
//    ret(ctx, si, cache);
//    ctx.vars.reset(vs);
//    if(order != null) {
//      order.sq = si;
//      return order.iter(ctx);
//    }
//    return si;
  }

  /**
   * Returns grouped vars.
   * @param ctx context.
   * @param si sequence to be filled.
   * @throws QueryException on error.
   */
  private void retG(final QueryContext ctx, final SeqIter si)
      throws QueryException {
    for(int i = 0; i < group.gp.partitions.size(); i++) { // bind grouping var
      HashMap<Var, ItemList> ngvars = group.gp.items.get(i);
      GroupNode gn = group.gp.partitions.get(i);
      for(int j = 0; j < gn.vars.length; j++) {
        gn.vars[j].bind(gn.its.get(j), ctx);
      }
      for(Var ngv : ngvars.keySet()) {
        final ItemList its = ngvars.get(ngv);
        ngv.bind(Seq.get(its.list, its.size()), ctx);
      }
      si.add(ctx.iter(ret).finish());
    }
  }

//  /**
//   * Evaluates return.
//   * @param ctx context.
//   * @param seq sequence to be filled.
//   * @param cache intermediate items
//   * @throws QueryException on error.
//   */
//  private void ret(final QueryContext ctx, final SeqIter seq,
//      final HashMap<Var, ItemList> cache) throws QueryException {
//    final ItemList al = cache.get(fl[0].var);
//    for(int i = 0; i < al.size(); i++) {
//      // bind outer variable
//      fl[0].var.bind(al.get(i), ctx);
//      for(int j = 1; j < fl.length; j++) {
//        final Item it = cache.get(fl[j].var).get(i);
//        fl[j].var.bind(it, ctx); // bind inner
//      }
//      seq.add(ctx.iter(ret).finish());
//
//    }
//  }

  /**
   * Performs a recursive iteration on the specified variable position.
   * @param ctx root reference
   * @param cache cached items
   * @param it iterator
   * @param p variable position
   * @throws QueryException query exception
   */
  private void iter(final QueryContext ctx,
      final HashMap<Var, ItemList> cache, final Iter[] it, final int p)
      throws QueryException {
    final boolean more = p + 1 != fl.length;
    while(it[p].next() != null) {
      if(more) {
        iter(ctx, cache, it, p + 1);
      } else {

        if(where == null || where.ebv(ctx).bool()) {
          for(ForLet aFl : fl) {
            if(order != null) cache.get(aFl.var).add(
                ctx.vars.get(aFl.var).item(ctx));
          }
          if(group != null)  group.add(ctx);
          if(order != null) order.add(ctx);
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
    return (where == null || where.removable(v, ctx))
        && (order == null || order.removable(v, ctx)) && ret.removable(v, ctx);
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
  public final SeqType returned(final QueryContext ctx) {
    return new SeqType(ret.returned(ctx).type, SeqType.Occ.ZM);
  }

  @Override
  public final String color() {
    return "66FF66";
  }

  @Override
  public final void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(final ForLet f : fl)
      f.plan(ser);
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
    for(int i = 0; i != fl.length; i++)
      sb.append(i != 0 ? " " : "").append(fl[i]);
    if(where != null) sb.append(" ").append(WHERE).append(" ").append(where);
    if(order != null) sb.append(order);
    if(group != null) sb.append(group);
    return sb.append(" ").append(RETURN).append(" ").append(ret).toString();
  }
}
