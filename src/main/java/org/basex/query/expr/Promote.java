package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Promote expression, used for type promotion on function arguments and results.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class Promote extends Single {
  /**
   * Constructor.
   * @param ii input info
   * @param e expression to be promoted
   * @param to type to promote to
   */
  private Promote(final InputInfo ii, final Expr e, final SeqType to) {
    super(ii, e);
    type = to;
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    expr = expr.compile(ctx, scp);
    return optPre(expr.isValue() ? value(ctx) : optimize(ctx, scp), ctx);
  }

  @Override
  public Expr optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    final SeqType argType = expr.type();
    if(argType.instanceOf(type)) return expr;
    // always matches item()
    if(argType.type.instanceOf(type.type)) {
      final SeqType.Occ occ = argType.occ.intersect(type.occ);
      if(occ == null) throw Err.XPTYPE.thrw(info, expr, type, argType);
      switch(occ) {
        case ZERO: break;
        case ONE:       return Function.EXACTLY_ONE.get(expr);
        case ZERO_ONE:  return Function.ZERO_OR_ONE.get(expr);
        case ONE_MORE:  return Function.ONE_OR_MORE.get(expr);
        case ZERO_MORE: throw Util.notexpected(this);
      }
    }
    return this;
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    return type.funcConvert(ctx, info, expr.value(ctx));
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
    return new Promote(info, expr.copy(ctx, scp, vs), type);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(QueryText.TYP, type), expr);
  }

  @Override
  public String toString() {
    return expr.toString() + " promote to " + type;
  }
}
