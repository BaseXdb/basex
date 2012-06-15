package org.basex.query.func;

import static java.lang.StrictMath.*;
import static org.basex.query.func.Function.*;

import java.util.*;
import java.util.zip.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Math functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNMath extends StandardFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNMath(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    double d = 0;
    if(expr.length > 0 && sig != _MATH_CRC32) {
      if(expr[0].isEmpty()) return null;
      d = checkDbl(expr[0], ctx);
    }
    final double e = expr.length == 2 ? checkDbl(expr[1], ctx) : 0;

    switch(sig) {
      case _MATH_PI:     return Dbl.get(PI);
      case _MATH_E:      return Dbl.get(E);
      case _MATH_SQRT:   return Dbl.get(sqrt(d));
      case _MATH_SIN:    return Dbl.get(sin(d));
      case _MATH_COS:    return Dbl.get(cos(d));
      case _MATH_TAN:    return Dbl.get(tan(d));
      case _MATH_ASIN:   return Dbl.get(asin(d));
      case _MATH_ACOS:   return Dbl.get(acos(d));
      case _MATH_ATAN:   return Dbl.get(atan(d));
      case _MATH_EXP:    return Dbl.get(exp(d));
      case _MATH_EXP10:  return Dbl.get(pow(10, d));
      case _MATH_LOG:    return Dbl.get(log(d));
      case _MATH_LOG10:  return Dbl.get(log10(d));
      case _MATH_ATAN2:  return Dbl.get(atan2(d, e));
      case _MATH_POW:    return Dbl.get(power(d, e));
      // project-specific
      case _MATH_SINH:   return Dbl.get(sinh(d));
      case _MATH_COSH:   return Dbl.get(cosh(d));
      case _MATH_TANH:   return Dbl.get(tanh(d));
      case _MATH_RANDOM: return Dbl.get(random());
      case _MATH_UUID:   return Str.get(UUID.randomUUID());
      case _MATH_CRC32:  return crc32(ctx);
      default:           return super.item(ctx, ii);
    }
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
      if(Double.isNaN(e)) return b;
      if(Double.isInfinite(e)) return 1;
    }
    return pow(b, e);
  }

  /**
   * Creates the CRC32 hash of the given xs:string.
   * @param ctx query context
   * @return xs:hexBinary instance containing the hash
   * @throws QueryException exception
   */
  private Hex crc32(final QueryContext ctx) throws QueryException {
    final CRC32 crc = new CRC32();
    crc.update(checkStr(expr[0], ctx));
    final byte[] r = new byte[4];
    for(int i = r.length, c = (int) crc.getValue(); i-- > 0; c >>>= 8) r[i] = (byte) c;
    return new Hex(r);
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X30 || u == Use.NDT && oneOf(sig, _MATH_RANDOM, _MATH_UUID) ||
        super.uses(u);
  }
}
