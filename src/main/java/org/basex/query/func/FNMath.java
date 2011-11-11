package org.basex.query.func;

import static java.lang.StrictMath.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.util.InputInfo;

/**
 * Math functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNMath extends FuncCall {
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
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    double d = 0;
    if(expr.length > 0) {
      if(expr[0].empty()) return null;
      d = checkDbl(expr[0], ctx);
    }
    final double e = expr.length > 1 ? checkDbl(expr[1], ctx) : 0;

    switch(def) {
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
      case _MATH_POW:    return Dbl.get(d == 1 ? 1 : pow(d, e));
      // project-specific
      case _MATH_RANDOM: return Dbl.get(random());
      case _MATH_SINH:   return Dbl.get(sinh(d));
      case _MATH_COSH:   return Dbl.get(cosh(d));
      case _MATH_TANH:   return Dbl.get(tanh(d));
      default:           return super.item(ctx, ii);
    }
  }

  @Override
  public boolean uses(final Use u) {
    // random() is non-deterministic; don't pre-evaluate
    return u == Use.X30 || u == Use.NDT && def == Function._MATH_RANDOM ||
      super.uses(u);
  }
}
