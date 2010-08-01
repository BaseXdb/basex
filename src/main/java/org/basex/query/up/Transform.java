package org.basex.query.up;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryInfo;
import org.basex.query.expr.Arr;
import org.basex.query.expr.Expr;
import org.basex.query.expr.ForLet;
import org.basex.query.expr.Let;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.up.primitives.UpdatePrimitive;

/**
 * Transform expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Kircher
 */
public final class Transform extends Arr {
  /** Variable bindings created by copy clause. */
  private final Let[] copies;

  /**
   * Constructor.
   * @param i query info
   * @param c copy expressions
   * @param m modify expression
   * @param r return expression
   */
  public Transform(final QueryInfo i, final Let[] c, final Expr m,
      final Expr r) {
    super(i, m, r);
    copies = c;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    final boolean u = ctx.updating;
    ctx.updating = true;

    final int s = ctx.vars.size();
    for(final Let c : copies) {
      c.expr = checkUp(c.expr, ctx).comp(ctx);
      c.var.ret = c.expr.returned(ctx);
      ctx.vars.add(c.var);
    }
    for(int e = 0; e != expr.length; e++) expr[e] = expr[e].comp(ctx);

    if(!expr[0].uses(Use.UPD, ctx) && !expr[0].vacuous()) error(UPEXPECTT);
    checkUp(expr[1], ctx);
    ctx.vars.reset(s);
    ctx.updating = u;
    return this;
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    final int s = ctx.vars.size();
    final Updates pu = new Updates(true);
    for(final Let fo : copies) {
      final Iter ir = fo.expr.iter(ctx);
      final Item i = ir.next();
      if(i == null || !i.node() || ir.next() != null) error(UPCOPYMULT);
      final Data m = UpdatePrimitive.buildDB(
          new NodIter(new Nod[] { (Nod) i }, 1), new MemData(ctx.context.prop));
      ctx.vars.add(fo.var.bind(new DBNode(m, 0), ctx).copy());
      pu.addDataReference(m);
    }

    final Updates tmp = ctx.updates;
    ctx.updates = pu;
    ctx.iter(expr[0]).finish();
    ctx.updates.apply(ctx);
    ctx.updates = tmp;

    final Item im = expr[1].iter(ctx).finish();
    ctx.vars.reset(s);
    return im;
  }

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    return u == Use.VAR || u != Use.UPD && super.uses(u, ctx);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(final Expr c : copies) c.plan(ser);
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(COPY + ' ');
    for(final ForLet t : copies)
      sb.append(t.var + " " + ASSIGN + ' ' + t.expr + ' ');
    return sb.append(MODIFY + ' ' + expr[0] + ' ' + RETURN + ' ' +
        expr[1]).toString();
  }
}
