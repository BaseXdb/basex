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
      case PI:    return Dbl.get(PI);
      case E:     return Dbl.get(E);
      case SQRT:  return Dbl.get(sqrt(d));
      case SIN:   return Dbl.get(sin(d));
      case COS:   return Dbl.get(cos(d));
      case TAN:   return Dbl.get(tan(d));
      case ASIN:  return Dbl.get(asin(d));
      case ACOS:  return Dbl.get(acos(d));
      case ATAN:  return Dbl.get(atan(d));
      case EXP:   return Dbl.get(exp(d));
      case EXP10: return Dbl.get(pow(10, d));
      case LOG:   return Dbl.get(log(d));
      case LOG10: return Dbl.get(log10(d));
      case ATAN2: return Dbl.get(atan2(d, e));
      case POW:   return Dbl.get(d == 1 ? 1 : pow(d, e));
      // project-specific
      case RANDOM:  return Dbl.get(random());
      case SINH:  return Dbl.get(sinh(d));
      case COSH:  return Dbl.get(cosh(d));
      case TANH:  return Dbl.get(tanh(d));
      default:    return super.item(ctx, ii);
    }
  }

  @Override
  public boolean uses(final Use u) {
    // random() is non-deterministic; don't pre-evaluate
    return u == Use.X30 || u == Use.NDT && def == Function.RANDOM ||
      super.uses(u);
  }
}
