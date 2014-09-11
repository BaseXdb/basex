package org.basex.query.func.math;

import static java.lang.StrictMath.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Math functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class MathPow extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it = exprs[0].atomItem(qc, info);
    return it == null ? null : Dbl.get(power(toDouble(it), toDouble(exprs[1], qc)));
  }

  /**
   * Calculates the power.
   * @param b base
   * @param e exponent
   * @return power
   */
  private static double power(final double b, final double e) {
    if(b == 1) return 1;
    if(b == -1) {
      if(Double.isNaN(e)) return -1;
      if(Double.isInfinite(e)) return 1;
    }
    return pow(b, e);
  }
}
