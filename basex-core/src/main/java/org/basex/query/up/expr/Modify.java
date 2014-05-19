package org.basex.query.up.expr;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Modify expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Modify extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param src source expression
   * @param mod modify expression
   */
  public Modify(final InputInfo info, final Expr src, final Expr mod) {
    super(info, src, mod);
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    final Value v = ctx.value;
    try {
      ctx.value = null;
      super.compile(ctx, scp);
      return this;
    } finally {
      ctx.value = v;
    }
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(expr[0]);
    final Expr m = expr[1];
    m.checkUp();
    if(!m.isVacuous() && !m.has(Flag.UPD)) throw UPMODIFY.get(info);
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

    final Value cv = ctx.value;
    try {
      final Iter ir = ctx.iter(expr[0]);
      Item i = ir.next();
      if(!(i instanceof ANode) || ir.next() != null) throw UPSOURCE.get(info);

      // copy node to main memory data instance
      i = ((ANode) i).dbCopy(ctx.context.options);
      // set resulting node as context
      ctx.value = i;
      pu.addData(i.data());

      ctx.value(expr[1]);
      ctx.updates.apply();
      return ctx.value;
    } finally {
      ctx.output.size(o);
      ctx.updates.mod = tmp;
      ctx.value = cv;
    }
  }

  @Override
  public boolean has(final Flag flag) {
    return flag != Flag.UPD && super.has(flag);
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new Modify(info, expr[0].copy(ctx, scp, vs), expr[1].copy(ctx, scp, vs));
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), expr);
  }

  @Override
  public String toString() {
    return toString(" update ");
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Expr e : expr) sz += e.exprSize();
    return sz;
  }
}
