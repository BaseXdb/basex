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
    seqType = SeqType.ITEM_ZO;
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    final SeqType st1 = exprs[0].seqType();
    final SeqType st2 = exprs[1].seqType();
    final Type t1 = st1.type, t2 = st2.type;
    final boolean o1 = st1.one() && !st1.mayBeArray();
    final boolean o2 = st2.one() && !st2.mayBeArray();
    if(t1.isNumberOrUntyped() && t2.isNumberOrUntyped()) {
      final Occ occ = o1 && o2 ? Occ.ONE : Occ.ZERO_ONE;
      seqType = SeqType.get(Calc.type(t1, t2), occ);
    } else if(o1 && o2) {
      seqType = SeqType.ITEM;
    }
    return optPre(oneIsEmpty() ? null : allAreValues() ? item(qc, info) : this, qc);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    Item it1 = exprs[0].item(qc, ii);
    if(it1 != null) it1 = it1.atomItem(ii);
    if(it1 == null) return null;
    Item it2 = exprs[1].item(qc, ii);
    if(it2 != null) it2 = it2.atomItem(ii);
    if(it2 == null) return null;
    return calc.ev(info, it1, it2);
  }

  @Override
  public Arith copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return copyType(new Arith(info, exprs[0].copy(qc, scp, vs), exprs[1].copy(qc, scp, vs), calc));
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
