package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * Case expression for typeswitch.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class TypeCase extends Single {
  /** Variable. */
  final Var var;

  /**
   * Constructor.
   * @param ii input info
   * @param v variable
   * @param r return expression
   */
  public TypeCase(final InputInfo ii, final Var v, final Expr r) {
    super(ii, r);
    var = v;
  }

  @Override
  public TypeCase compile(final QueryContext ctx) throws QueryException {
    return compile(ctx, null);
  }

  /**
   * Compiles the expression.
   * @param ctx query context
   * @param v value to be bound
   * @return resulting item
   * @throws QueryException query exception
   */
  TypeCase compile(final QueryContext ctx, final Value v) throws QueryException {
    if(var.name == null) {
      super.compile(ctx);
    } else {
      final int s = ctx.vars.size();
      ctx.vars.add(v == null ? var : var.bind(v, ctx).copy());
      super.compile(ctx);
      ctx.vars.size(s);
    }
    type = expr.type();
    return this;
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.VAR || super.uses(u);
  }

  /**
   * Evaluates the expression.
   * @param ctx query context
   * @param seq sequence to be checked
   * @return resulting item
   * @throws QueryException query exception
   */
  Iter iter(final QueryContext ctx, final Value seq) throws QueryException {
    if(var.type != null && !var.type.instance(seq)) return null;
    if(var.name == null) return ctx.iter(expr);

    final int s = ctx.vars.size();
    ctx.vars.add(var.bind(seq, ctx).copy());
    final ValueIter ic = ctx.value(expr).iter();
    ctx.vars.size(s);
    return ic;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(VAR, var.name != null ? var.name.string() : ""), expr);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(var.type == null ? DEFAULT : CASE);
    if(var.name != null) tb.add(' ');
    return tb.add(var + " " + RETURN + ' ' + expr).toString();
  }

  @Override
  public TypeCase markTailCalls() {
    expr = expr.markTailCalls();
    return this;
  }
}
