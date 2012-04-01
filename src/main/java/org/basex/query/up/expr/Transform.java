package org.basex.query.up.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.io.IOException;
import org.basex.data.MemData;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Arr;
import org.basex.query.expr.Expr;
import org.basex.query.flwor.Let;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ValueIter;
import org.basex.query.up.ContextModifier;
import org.basex.query.up.TransformModifier;
import org.basex.query.util.DataBuilder;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Transform expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class Transform extends Arr {
  /** Variable bindings created by copy clause. */
  private final Let[] copies;

  /**
   * Constructor.
   * @param ii input info
   * @param c copy expressions
   * @param m modify expression
   * @param r return expression
   */
  public Transform(final InputInfo ii, final Let[] c, final Expr m,
      final Expr r) {
    super(ii, m, r);
    copies = c;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    final boolean u = ctx.updating();
    ctx.updating(true);

    final int s = ctx.vars.size();
    for(final Let c : copies) {
      c.expr = checkUp(c.expr, ctx).comp(ctx);
      ctx.vars.add(c.var);
    }
    for(int e = 0; e != expr.length; ++e) expr[e] = expr[e].comp(ctx);

    if(!expr[0].uses(Use.UPD) && !expr[0].isVacuous()) UPEXPECTT.thrw(info);
    checkUp(expr[1], ctx);
    ctx.vars.size(s);
    ctx.updating(u);
    return this;
  }

  @Override
  public ValueIter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    final int s = ctx.vars.size();
    try {
      final TransformModifier pu = new TransformModifier();
      for(final Let fo : copies) {
        final Iter ir = ctx.iter(fo.expr);
        final Item i = ir.next();
        if(i == null || !i.type.isNode() || ir.next() != null)
          UPCOPYMULT.thrw(info);

        // copy node to main memory data instance
        final MemData md = new MemData(ctx.context.prop);
        new DataBuilder(md).build((ANode) i);

        // add resulting node to variable
        ctx.vars.add(fo.var.bind(new DBNode(md), ctx).copy());
        pu.addData(md);
      }

      final ContextModifier tmp = ctx.updates.mod;
      ctx.updates.mod = pu;
      ctx.value(expr[0]);
      ctx.updates.apply();
      ctx.updates.mod = tmp;

      return ctx.value(expr[1]);
    } finally {
      ctx.vars.size(s);
    }
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.VAR || u != Use.UPD && super.uses(u);
  }

  @Override
  public int count(final Var v) {
    int c = 0;
    for(final Let l : copies) c += l.count(v);
    return c + super.count(v);
  }

  @Override
  public boolean removable(final Var v) {
    for(final Let c : copies) if(!c.removable(v)) return false;
    return super.removable(v);
  }

  @Override
  public Expr remove(final Var v) {
    for(final Let c : copies) c.remove(v);
    return super.remove(v);
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
    for(final Let t : copies)
      sb.append(t.var + " " + ASSIGN + ' ' + t.expr + ' ');
    return sb.append(MODIFY + ' ' + expr[0] + ' ' + RETURN + ' ' +
        expr[1]).toString();
  }
}
