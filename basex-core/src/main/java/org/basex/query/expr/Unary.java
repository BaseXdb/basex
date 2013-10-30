package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Unary expression.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class Unary extends Single {
  /** Minus flag. */
  private final boolean minus;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param min minus flag
   */
  public Unary(final InputInfo ii, final Expr e, final boolean min) {
    super(ii, e);
    minus = min;
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    super.compile(ctx, scp);
    type = expr.type();
    if(!type.type.isNumber()) {
      // expression will always yield a number, empty sequence or error
      type = type.mayBeZero() ? SeqType.ITR_ZO : SeqType.ITR;
    }
    return expr.isValue() ? preEval(ctx) : this;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Item it = expr.item(ctx, info);
    if(it == null) return null;
    final Type ip = it.type;

    if(!ip.isNumberOrUntyped()) Err.number(this, it);
    final double d = it.dbl(info);
    if(ip.isUntyped()) return Dbl.get(minus ? -d : d);

    if(!minus) return it;
    switch((AtomType) ip) {
      case DBL: return Dbl.get(-d);
      case FLT: return Flt.get(-it.flt(info));
      case DEC: return Dec.get(it.dec(info).negate());
      default:
        final long l = it.itr(info);
        if(l == Long.MIN_VALUE) Err.RANGE.thrw(info, it);
        return Int.get(-l);
    }
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return copyType(new Unary(info, expr.copy(ctx, scp, vs), minus));
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(VAL, minus), expr);
  }

  @Override
  public String toString() {
    return (minus ? "-" : "") + expr;
  }
}
