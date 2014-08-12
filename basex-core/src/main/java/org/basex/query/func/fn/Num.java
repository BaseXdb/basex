package org.basex.query.func.fn;

import java.math.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Numeric functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class Num extends StandardFunc {
  /**
   * Returns a rounded item.
   * @param it input item
   * @param d input double value
   * @param h2e half-to-even flag
   * @param qc query context
   * @return absolute item
   * @throws QueryException query exception
   */
  protected Item rnd(final Item it, final double d, final boolean h2e, final QueryContext qc)
      throws QueryException {
    final long p = exprs.length == 1 ? 0 : toLong(exprs[1], qc);
    return round(it, d, p, h2e, info);
  }

  /**
   * Returns a rounded item.
   * @param it input item
   * @param d input double value
   * @param h2e half-to-even flag
   * @param prec precision
   * @param ii input info
   * @return absolute item
   * @throws QueryException query exception
   */
  public static ANum round(final Item it, final double d, final long prec, final boolean h2e,
      final InputInfo ii) throws QueryException {

    // take care of untyped items
    final Item num = it.type.isUntyped() ? Dbl.get(it.dbl(ii)) : it;

    if(num.type == AtomType.DEC && prec >= 0) {
      final BigDecimal bd = num.dec(ii);
      final int m = h2e ? BigDecimal.ROUND_HALF_EVEN : bd.signum() > 0 ?
          BigDecimal.ROUND_HALF_UP : BigDecimal.ROUND_HALF_DOWN;
      return Dec.get(prec > Integer.MAX_VALUE ? bd : bd.setScale((int) prec, m));
    }

    double c = d;
    if(!Double.isNaN(c) && !Double.isInfinite(c) && prec < 32) {
      // calculate precision factor
      double p = 1;
      for(long i = prec; i > 0; --i) p *= 10;
      for(long i = prec; i < 0; ++i) p /= 10;

      if(h2e) {
        c *= p;
        if(d < 0) c = -c;
        final double r = c % 1;
        c += r == .5 ? c % 2 == 1.5 ? .5 : -.5 : r > .5 ? 1 - r : -r;
        c /= p;
        if(d < 0) c = -c;
      } else if(c >= Long.MIN_VALUE && c < Long.MAX_VALUE) {
        final double dp = d * p;
        c = (dp >= -.5d && dp < 0 ? -0d : StrictMath.round(dp)) / p;
      }
    }
    return num(it, d, c);
  }

  /**
   * Returns a numeric result with the correct type.
   * @param it input item
   * @param n input double value
   * @param d calculated double value
   * @return numeric item
   */
  private static ANum num(final Item it, final double n, final double d) {
    final Type ip = it.type;
    final Item i = ip.isUntyped() ? Dbl.get(n) : it;
    if(n == d) return (ANum) i;

    if(ip instanceof AtomType) {
      switch((AtomType) ip) {
        case DEC: return Dec.get(d);
        case DBL: return Dbl.get(d);
        case FLT: return Flt.get((float) d);
        case ITR: return Int.get((long) d);
        default:  break;
      }
    }
    return Dbl.get(d);
  }
}
