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
import org.basex.query.util.ValueList;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * FLWOR clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class FLWOR extends ParseExpr {
  /** Expression list. */
  protected Expr ret;
  /** For/Let expressions. */
  protected ForLet[] fl;
  /** Where expression. */
  protected Expr where;
  /** Order expressions. */
  private Order order;

  /**
   * FLWOR initialization.
   * @param f variable inputs
   * @param w where clause
   * @param o order expression
   * @param r return expression
   * @param ii input info
   */
  public FLWOR(final ForLet[] f, final Expr w, final Order o, final Expr r,
      final InputInfo ii) {

    super(ii);
    ret = r;
    fl = f;
    where = w;
    order = o;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    final int vs = ctx.vars.size();

    // optimize for/let clauses
    for(int f = 0; f != fl.length; f++) {
      // disable fast full-text evaluation if score value exists
      final boolean fast = ctx.ftfast;
      ctx.ftfast &= fl[f].simple();
      fl[f].comp(ctx);
      ctx.ftfast = fast;
    }

    // optimize where clause
    boolean empty = false;
    if(where != null) {
      where = checkUp(where, ctx).comp(ctx);
      if(where.value()) {
        // test is always false: no results
        empty = !where.ebv(ctx, input).bool(input);
        if(!empty) {
          // always true: test can be skipped
          ctx.compInfo(OPTTRUE, where);
          where = null;
        }
      }
    }

    if(order != null) order.comp(ctx);
    ret = ret.comp(ctx);

    ctx.vars.reset(vs);

    if(empty) {
      ctx.compInfo(OPTFALSE, where);
      return Empty.SEQ;
    }

    for(final ForLet f : fl) {
      // remove FLWOR expression if a FOR clause yields an empty sequence
      if(f.expr.empty() && f instanceof For) {
        ctx.compInfo(OPTFLWOR);
        return Empty.SEQ;
      }
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final ValueList vl = new ValueList();
    final Iter[] iter = new Iter[fl.length];
    for(int f = 0; f < fl.length; f++) iter[f] = ctx.iter(fl[f]);
    iter(ctx, vl, iter, 0);
    order.vl = vl;
    return ctx.iter(order);
  }

  /**
   * Performs a recursive iteration on the specified variable position.
   * @param ctx root reference
   * @param vl value lists
   * @param it iterators
   * @param p variable position
   * @throws QueryException query exception
   */
  private void iter(final QueryContext ctx, final ValueList vl,
      final Iter[] it, final int p) throws QueryException {

    final boolean more = p + 1 != fl.length;
    while(it[p].next() != null) {
      if(more) {
        iter(ctx, vl, it, p + 1);
      } else {
        if(where == null || where.ebv(ctx, input).bool(input)) {
          order.add(ctx);
          vl.add(ctx.iter(ret).finish());
        }
      }
    }
  }

  @Override
  public long size(final QueryContext ctx) throws QueryException {
    // don't test where clause (order clause doesn't change result size)
    if(where != null) return -1;
    // check if number of results of return clause is known
    long size = ret.size(ctx);
    if(size == -1) return -1;
    // multiply loop runs
    for(final ForLet f : fl) {
      final long s = f.size(ctx);
      if(s == -1) return -1;
      size *= s;
    }
    return size;
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
    for(final ForLet f : fl) f.plan(ser);
    if(where != null) {
      ser.openElement(WHR);
      where.plan(ser);
      ser.closeElement();
    }
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
    return sb.append(" " + RETURN + " " + ret).toString();
  }
}
