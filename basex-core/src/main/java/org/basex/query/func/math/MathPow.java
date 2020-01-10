package org.basex.query.func.math;

import static java.lang.StrictMath.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class MathPow extends MathFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = exprs[0].atomItem(qc, info);
    return item == Empty.VALUE ? Empty.VALUE :
      Dbl.get(power(toDouble(item), toDouble(exprs[1], qc)));
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
