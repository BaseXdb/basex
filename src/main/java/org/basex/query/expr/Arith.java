package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.util.*;

/**
 * Arithmetic expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Arith extends Arr {
  /** Calculation operator. */
  private final Calc calc;

  /**
   * Constructor.
   * @param ii input info
   * @param e1 first expression
   * @param e2 second expression
   * @param c calculation operator
   */
  public Arith(final InputInfo ii, final Expr e1, final Expr e2, final Calc c) {
    super(ii, e1, e2);
    calc = c;
  }

  @Override
  public Expr compile(final QueryContext ctx) throws QueryException {
    super.compile(ctx);

    final SeqType s0 = expr[0].type();
    final SeqType s1 = expr[1].type();
    final boolean t1 = s0.type.isNumber() || s0.type.isUntyped();
    final boolean t2 = s0.type.isNumber() || s1.type.isUntyped();
    if(t1 && t2) {
      type = s0.one() && s1.one() ? SeqType.ITR : SeqType.ITR_ZO;
    } else if(s0.one() && s1.one()) {
      type = SeqType.ITEM;
    } else {
      type = SeqType.ITEM_ZO;
    }
    return optPre(oneIsEmpty() ? null : allAreValues() ? item(ctx, info) : this, ctx);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Item a = expr[0].item(ctx, info);
    if(a == null) return null;
    final Item b = expr[1].item(ctx, info);
    if(b == null) return null;
    return calc.ev(info, a, b);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(OP, calc.name), expr);
  }

  @Override
  public String description() {
    return '\'' + calc.name + "' expression";
  }

  @Override
  public String toString() {
    return toString(' ' + calc.name + ' ');
  }
}
