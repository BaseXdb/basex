package org.basex.query.up;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Arr;
import org.basex.query.expr.Constr;
import org.basex.query.expr.Expr;
import org.basex.query.expr.ForLet;
import org.basex.query.expr.Let;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.util.Err;

/**
 * Transform expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class Transform extends Arr {
  /** Variable bindings created by copy clause. */
  private final Let[] copies;

  // [LK] modified a little. still work in progress, though.

  /**
   * Constructor.
   * @param c copy expressions
   * @param m modify expression
   * @param r return expression
   */
  public Transform(final Let[] c, final Expr m, final Expr r) {
    super(m, r);
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

    if(!expr[0].uses(Use.UPD, ctx) && !expr[0].v()) Err.or(UPEXPECT);
    checkUp(expr[1], ctx);
    ctx.vars.reset(s);

    ctx.updating = u;
    return this;
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    final int s = ctx.vars.size();
    final PendingUpdates tUpd = new PendingUpdates(true);
    for(final Let fo : copies) {
      final Constr c = new Constr(ctx, fo.expr);
      final int cs = c.children.size();
      final int os = cs + c.ats.size();
      if(os != 1) Err.or(UPCOPYMULT, this);
      final Data m = UpdateFunctions.buildDB(cs > 0 ? c.children : c.ats, null);
      ctx.vars.add(fo.var.bind(new DBNode(m, 0), ctx).copy());
      tUpd.addDataReference(m);
    }

    final PendingUpdates upd = ctx.updates;
    ctx.updates = tUpd;
    ctx.iter(expr[0]).finish();
    ctx.updates.apply();
    ctx.updates = upd;

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
    return sb.append(MODIFY + ' ' + expr[0] + ' '  + RETURN + ' ' +
        expr[1]).toString();
  }
}
