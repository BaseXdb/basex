package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Checks the argument expression's result type.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public final class TypeCheck extends Single {
  /** Flag for function conversion. */
  public final boolean promote;
  /**
   * Constructor.
   * @param ii input info
   * @param e expression to be promoted
   * @param to type to promote to
   * @param f flag for function conversion
   */
  public TypeCheck(final InputInfo ii, final Expr e, final SeqType to, final boolean f) {
    super(ii, e);
    type = to;
    promote = f;
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    expr = expr.compile(ctx, scp);
    return optimize(ctx, scp);
  }

  @Override
  public Expr optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    if(expr.isValue()) return optPre(value(ctx), ctx);

    final SeqType argType = expr.type();
    if(argType.instanceOf(type)) {
      ctx.compInfo(QueryText.OPTCAST, type);
      return expr;
    }

    if(argType.type.instanceOf(type.type)) {
      final SeqType.Occ occ = argType.occ.intersect(type.occ);
      if(occ == null) throw Err.INVCAST.thrw(info, argType, type);
    }

    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    final Value val = expr.value(ctx);
    if(type.instance(val)) return val;
    if(promote) return type.funcConvert(ctx, info, val);
    throw Err.INVCAST.thrw(info, val.type(), type);
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new TypeCheck(info, expr.copy(ctx, scp, vs), type, promote);
  }

  @Override
  public void plan(final FElem plan) {
    final FElem elem = planElem(QueryText.TYP, type);
    if(promote) elem.add(planAttr(QueryText.FUNCTION, Token.TRUE));
    addPlan(plan, elem, expr);
  }

  @Override
  public String toString() {
    return "((: " + type + ", " + promote + " :) " + expr.toString() + ")";
  }

  /**
   * Checks if this type check is redundant if the result is bound to the given variable.
   * @param var variable
   * @return result of check
   */
  public boolean isRedundant(final Var var) {
    return (!promote || var.promotes()) && var.declaredType().instanceOf(type);
  }
}
