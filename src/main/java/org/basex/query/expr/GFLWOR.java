package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Empty;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ItemIter;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * GFLWOR clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class GFLWOR extends ParseExpr {
  /** Expression list. */
  Expr ret;
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
   * @param ii input info
   */
  public GFLWOR(final ForLet[] f, final Expr w, final Order o, final Group g,
      final Expr r, final InputInfo ii) {

    super(ii);
    ret = r;
    fl = f;
    where = w;
    group = g;
    order = o;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    final boolean grp = ctx.grouping;
    ctx.grouping = group != null;
    final int vs = ctx.vars.size();

    // optimize for/let clauses
    for(int f = 0; f != fl.length; ++f) {
      // disable fast full-text evaluation if score value exists
      final boolean fast = ctx.ftfast;
      ctx.ftfast &= fl[f].simple();
      fl[f] = fl[f].comp(ctx);
      ctx.ftfast = fast;
    }

    // optimize where clause
    boolean empty = false;
    if(where != null) {
      where = checkUp(where.comp(ctx), ctx);
      if(where.value()) {
        // test is always false: no results
        empty = !where.ebv(ctx, input).bool(input);
        if(!empty) {
          // always true: test can be skipped
          ctx.compInfo(OPTREMOVE, desc(), where);
          where = null;
        }
      }
    }

    if(group != null) group.comp(ctx);
    if(order != null) order.comp(ctx);
    ret = ret.comp(ctx);

    ctx.vars.reset(vs);
    ctx.grouping = grp;

    if(empty) {
      ctx.compInfo(OPTREMOVE, desc(), where);
      return Empty.SEQ;
    }

    for(int f = 0; f != fl.length; ++f) {
      // remove GFLWOR expression if a FOR clause yields an empty sequence
      if(fl[f].expr.empty() && fl[f] instanceof For) {
        ctx.compInfo(OPTFLWOR);
        return Empty.SEQ;
      }
    }

    type = new SeqType(ret.type().type, SeqType.Occ.ZM);
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Iter[] iter = new Iter[fl.length];
    final int vss = ctx.vars.size();

    buildIter(iter, ctx);
    
    // evaluate pre grouping tuples
    group.initgroup(fl);
    iter(ctx, iter, 0);
    ctx.vars.reset(vss);

    final int vs = ctx.vars.size();
    for(final ForLet aFl : fl) ctx.vars.add(aFl.var);

    final ItemIter ir = new ItemIter();
    group.ret(ctx, ir, ret);
    ctx.vars.reset(vs);
    return ir;

  }

  /**
   * Builds the iter array.
   * @param iter iterator
   * @param ctx querycontext
   * @throws QueryException exception
   */
  private void buildIter(final Iter[] iter, final QueryContext ctx)
      throws QueryException {
    // bitmap indicates which grouping variables have been added to iter
    boolean[] vars = new boolean[fl.length];
    
    for(int f = 0; f < group.groupby.length; ++f) { // fill each var with data
      for(int p = 0; p < fl.length; ++p) {
        if(fl[p].var.eq(group.groupby[f])) {
          iter[f] = ctx.iter(fl[p]);
          vars[p] = true;
        }
      } 
    }
    for(int f = 0; f < iter.length; ++f) {
      if(iter[f] != null) continue;
      for(int p = 0; p < fl.length; ++p) {
        if(vars[p]) continue;
        iter[f] = ctx.iter(fl[p]);
        vars[p] = true;
        break;

      }
    }
  }

  /**
   * Performs a recursive iteration on the specified variable position.
   * @param ctx root reference
   * @param it iterator
   * @param p variable position
   * @throws QueryException query exception
   */
  private void iter(final QueryContext ctx,
       final Iter[] it, final int p)
      throws QueryException {
    final boolean more = p + 1 != fl.length;
    while(it[p].next() != null) {
      if(more) {
        iter(ctx, it, p + 1);
      } else {
        if(where == null || where.ebv(ctx, input).bool(input)) {
//          for(final ForLet aFl : fl) {
//            if(order != null) {
//            [MS] check if values are always single items (and no sequences)
//            cache.get(aFl.var).add((Item) ctx.vars.get(aFl.var).value(ctx));
//            }
//          }
          if(group != null) group.add(ctx);
          if(order != null) order.add(ctx);
        }
      }
    }
  }

  @Override
  public final boolean uses(final Use u) {
    return u == Use.VAR || ret.uses(u);
  }

  @Override
  public final boolean removable(final Var v) {
    for(final ForLet f : fl) {
      if(!f.removable(v)) return false;
      if(f.shadows(v)) return true;
    }
    return (where == null || where.removable(v))
        && (order == null || order.removable(v))
        && (group == null || group.removable(v)) && ret.removable(v);
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
    for(int i = 0; i != fl.length; ++i)
      sb.append(i != 0 ? " " : "").append(fl[i]);
    if(where != null) sb.append(" ").append(WHERE).append(" ").append(where);
    if(order != null) sb.append(order);
    if(group != null) sb.append(group);
    return sb.append(" ").append(RETURN).append(" ").append(ret).toString();
  }
}
