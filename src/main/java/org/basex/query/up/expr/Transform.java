package org.basex.query.up.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.flwor.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

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
  public Transform(final InputInfo ii, final Let[] c, final Expr m, final Expr r) {
    super(ii, m, r);
    copies = c;
  }

  @Override
  public void checkUp() throws QueryException {
    for(final Let c : copies) c.checkUp();
    if(!expr[0].isVacuous() && !expr[0].uses(Use.UPD)) UPEXPECTT.thrw(info);
    checkNoUp(expr[1]);
  }

  @Override
  public Expr compile(final QueryContext ctx) throws QueryException {
    final int s = ctx.vars.size();
    for(final Let c : copies) {
      c.expr = c.expr.compile(ctx);
      ctx.vars.add(c.var);
    }
    super.compile(ctx);
    ctx.vars.size(s);
    return this;
  }

  @Override
  public ValueIter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    final int s = ctx.vars.size();
    final int o = (int) ctx.output.size();
    final ContextModifier tmp = ctx.updates.mod;
    final TransformModifier pu = new TransformModifier();
    ctx.updates.mod = pu;

    try {
      for(final Let fo : copies) {
        final Iter ir = ctx.iter(fo.expr);
        Item i = ir.next();
        if(!(i instanceof ANode) || ir.next() != null) UPCOPYMULT.thrw(info);

        // copy node to main memory data instance
        i = ((ANode) i).dbCopy(ctx.context.prop);
        // add resulting node to variable
        ctx.vars.add(fo.var.bind(i, ctx).copy());
        pu.addData(i.data());
      }
      ctx.value(expr[0]);
      ctx.updates.apply();
      return ctx.value(expr[1]);
    } finally {
      ctx.vars.size(s);
      ctx.output.size(o);
      ctx.updates.mod = tmp;
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
  public boolean databases(final StringList db) {
    for(final Let c : copies) if(!c.databases(db)) return false;
    return super.databases(db);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), copies, expr);
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
