package org.basex.query.func.math;

import static java.lang.StrictMath.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MathPow extends MathFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Double x = toDoubleOrNull(arg(0), qc);
    final double y = toDouble(arg(1), qc);
    return x == null ? Empty.VALUE : Dbl.get(power(x, y));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr base = arg(0), exp = arg(1);
    final SeqType st = base.seqType(), est = exp.seqType();

    // math:pow(1, y) → 1
    if(base instanceof final ANum num && num.dbl() == 1 && est.one() &&
        est.type.isNumberOrUntyped()) return Dbl.ONE;

    if(exp instanceof final ANum num && st.type.isNumberOrUntyped()) {
      final double e = num.dbl();
      if(e == 0 && st.one()) return Dbl.ONE;
      if(e == 1) return new Cast(info, base, Types.DOUBLE_ZO).optimize(cc);
      if(e == -1) return new Arith(info, Dbl.ONE, base, Calc.DIVIDE).optimize(cc);
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
    return exp == 2 ? base * base : pow(base, exp);
  }
}
