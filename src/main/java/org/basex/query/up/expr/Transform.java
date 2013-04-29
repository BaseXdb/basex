package org.basex.query.up.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.gflwor.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

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
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    for(final Let c : copies) c.expr = c.expr.compile(ctx, scp);
    super.compile(ctx, scp);
    return this;
  }

  @Override
  public ValueIter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    final int o = (int) ctx.output.size();
    if(ctx.updates == null) ctx.updates = new Updates();
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
        ctx.set(fo.var, i, info);
        pu.addData(i.data());
      }
      ctx.value(expr[0]);
      ctx.updates.apply();
      return ctx.value(expr[1]);
    } finally {
      ctx.output.size(o);
      ctx.updates.mod = tmp;
    }
  }

  @Override
  public boolean uses(final Use u) {
    return u != Use.UPD && super.uses(u);
  }

  @Override
  public boolean removable(final Var v) {
    for(final Let c : copies) if(!c.removable(v)) return false;
    return super.removable(v);
  }

  @Override
  public VarUsage count(final Var v) {
    return VarUsage.sum(v, copies).plus(super.count(v));
  }

  @Override
  public Expr inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    final boolean cp = inlineAll(ctx, scp, copies, v, e);
    return inlineAll(ctx, scp, expr, v, e) || cp ? optimize(ctx, scp) : null;
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
    return new Transform(info, copyAll(ctx, scp, vs, copies), expr[0].copy(ctx, scp, vs),
        expr[1].copy(ctx, scp, vs));
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), copies, expr);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(COPY + ' ');
    for(final Let t : copies) sb.append(t.var + " " + ASSIGN + ' ' + t.expr + ' ');
    return sb.append(MODIFY + ' ' + expr[0] + ' ' + RETURN + ' ' + expr[1]).toString();
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitAll(visitor, copies) && super.accept(visitor);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Let lt : copies) sz += lt.exprSize();
    for(final Expr e : expr) sz += e.exprSize();
    return sz;
  }
}
