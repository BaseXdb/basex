package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Arithmetic expression.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class ArithSimple extends Arith {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr1 first expression
   * @param expr2 second expression
   * @param calc calculation operator
   * @param opt optimized calculation
   */
  public ArithSimple(final InputInfo info, final Expr expr1, final Expr expr2, final Calc calc,
      final CalcOpt opt) {
    super(info, expr1, expr2, calc);
    calcOpt = opt;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item1 = exprs[0].item(qc, info);
    if(item1.isEmpty()) return Empty.VALUE;
    final Item item2 = exprs[1].item(qc, info);
    return item2.isEmpty() ? Empty.VALUE : calcOpt.eval(item1, item2, info);
  }

  @Override
  public ArithSimple copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new ArithSimple(info, exprs[0].copy(cc, vm),
        exprs[1].copy(cc, vm), calc, calcOpt));
  }

  @Override
  public String description() {
    return "simplified " + super.description();
  }
}
