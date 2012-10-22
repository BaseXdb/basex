package org.basex.query.func;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Partial function application.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class PartFunc extends UserFunc {
  /**
   * Function constructor for static calls.
   * @param ii input info
   * @param fun typed function expression
   * @param arg arguments
   * @param qc query context
   */
  public PartFunc(final InputInfo ii, final TypedFunc fun, final Var[] arg,
      final QueryContext qc) {
    super(ii, new QNm(), nn(fun.type.type(arg)), fun.ret(), null, qc);
    expr = fun.fun;
  }

  /**
   * Function constructor for dynamic calls.
   * @param ii input info
   * @param func function expression
   * @param arg arguments
   * @param ctx query context
   */
  public PartFunc(final InputInfo ii, final Expr func, final Var[] arg,
      final QueryContext ctx) {
    // [LW] XQuery/HOF: dynamic type propagation
    super(ii, new QNm(), nn(arg), func.type(), null, ctx);
    expr = func;
  }

  @Override
  public Expr compile(final QueryContext ctx) throws QueryException {
    compile(ctx, false);
    // defer creation of function item because of closure
    return new InlineFunc(info, ret, args, expr, ann, ctx).compile(ctx);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    return compile(ctx).item(ctx, ii);
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    return item(ctx, info);
  }

  @Override
  public void plan(final FElem plan) {
    final FElem el = planElem();
    addPlan(plan, el, expr);
    for(int i = 0; i < args.length; ++i) {
      el.add(planAttr(ARG + i, args[i].name.string()));
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(FUNCTION).append('(');
    for(final Var v : args)
      sb.append(v).append(v == args[args.length - 1] ? "" : ", ");
    return sb.append(") { ").append(expr).append(" }").toString();
  }

  /**
   * Collects all non-{@code null} variables from the array.
   * @param vars array of variables, can contain {@code null}s
   * @return all non-{@code null} variables
   */
  private static Var[] nn(final Var[] vars) {
    Var[] out = {};
    for(final Var v : vars) if(v != null) out = Array.add(out, v);
    return out;
  }

  @Override
  protected boolean tco() {
    return false;
  }
}
