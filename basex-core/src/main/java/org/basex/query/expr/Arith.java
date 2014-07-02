package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Arithmetic expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Arith extends Arr {
  /** Calculation operator. */
  private final Calc calc;

  /**
   * Constructor.
   * @param info input info
   * @param expr1 first expression
   * @param expr2 second expression
   * @param calc calculation operator
   */
  public Arith(final InputInfo info, final Expr expr1, final Expr expr2, final Calc calc) {
    super(info, expr1, expr2);
    this.calc = calc;
    type = SeqType.ITEM_ZO;
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    super.compile(ctx, scp);
    return optimize(ctx, scp);
  }

  @Override
  public Expr optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    final SeqType s0 = exprs[0].type();
    final SeqType s1 = exprs[1].type();
    final Type t0 = s0.type;
    final Type t1 = s1.type;
    if(t0.isNumberOrUntyped() && t1.isNumberOrUntyped()) {
      final Occ occ = s0.one() && s1.one() ? Occ.ONE : Occ.ZERO_ONE;
      type = SeqType.get(Calc.type(t0, t1), occ);
    } else if(s0.one() && s1.one()) {
      type = SeqType.ITEM;
    }
    return optPre(oneIsEmpty() ? null : allAreValues() ? item(ctx, info) : this, ctx);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Item a = exprs[0].item(ctx, info);
    if(a == null) return null;
    final Item b = exprs[1].item(ctx, info);
    if(b == null) return null;
    return calc.ev(info, a, b);
  }

  @Override
  public Arith copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    final Expr a = exprs[0].copy(ctx, scp, vs), b = exprs[1].copy(ctx, scp, vs);
    return copyType(new Arith(info, a, b, calc));
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(OP, calc.name), exprs);
  }

  @Override
  public String description() {
    return '\'' + calc.name + "' operator";
  }

  @Override
  public String toString() {
    return toString(' ' + calc.name + ' ');
  }
}
