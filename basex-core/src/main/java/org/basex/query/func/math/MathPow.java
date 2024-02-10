package org.basex.query.func.math;

import static java.lang.StrictMath.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class MathPow extends MathFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item x = arg(0).atomItem(qc, info);
    final double y = toDouble(arg(1), qc);
    return x.isEmpty() ? Empty.VALUE : Dbl.get(power(toDouble(x), y));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr base = arg(0);
    if(base instanceof ANum && ((ANum) base).dbl() == 1) return Dbl.ONE;

    final Expr exp = arg(1);
    if(exp instanceof ANum) {
      final double e = ((ANum) exp).dbl();
      if(e == 0) return Dbl.ONE;
      if(e == 1) return new Cast(sc, info, base, SeqType.DOUBLE_O).optimize(cc);
      if(e == -1) return new Arith(info, Dbl.ONE, base, Calc.DIVIDE).optimize(cc);
    }
    // merge nested function calls
    if(_MATH_POW.is(base)) {
      final Expr factor = new Arith(info, base.arg(1), exp, Calc.MULTIPLY).optimize(cc);
      return cc.function(_MATH_POW, info, base.arg(0), factor);
    }
    return super.opt(cc);
  }

  /**
   * Calculates the power.
   * @param base base
   * @param exp exponent
   * @return power
   */
  private static double power(final double base, final double exp) {
    if(base == 1) return 1;
    if(base == -1) {
      if(Double.isNaN(exp)) return -1;
      if(Double.isInfinite(exp)) return 1;
    }
    return pow(base, exp);
  }
}
