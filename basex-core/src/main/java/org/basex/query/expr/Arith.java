package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Arithmetic expression.
 *
 * @author BaseX Team 2005-18, BSD License
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
    super(info, SeqType.AAT_ZO, expr1, expr2);
    this.calc = calc;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
    final Type type1 = st1.type, type2 = st2.type;
    final boolean nums = type1.isNumberOrUntyped() && type2.isNumberOrUntyped();
    final Type type = calc == Calc.IDIV ? AtomType.ITR : nums ? Calc.type(type1, type2) :
      AtomType.AAT;
    exprType.assign(type, st1.oneNoArray() && st2.oneNoArray() ? Occ.ONE : Occ.ZERO_ONE);

    Expr expr = this;
    if(oneIsEmpty()) {
      expr = cc.emptySeq(this);
    } else if(allAreValues(false)) {
      expr = value(cc.qc);
    } else if(nums && st1.oneNoArray() && st2.oneNoArray()) {
      expr = calc.optimize(expr1, expr2);
      if(expr == null || !expr.seqType().type.eq(type)) expr = this;
    }
    return cc.replaceWith(this, expr);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item1 = exprs[0].atomItem(qc, info);
    if(item1 == null) return null;
    final Item item2 = exprs[1].atomItem(qc, info);
    return item2 == null ? null : calc.eval(item1, item2, info);
  }

  @Override
  public Arith copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Arith(info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), calc));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Arith && calc == ((Arith) obj).calc && super.equals(obj);
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
