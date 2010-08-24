package org.basex.query.func;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.util.InputInfo;

/**
 * Math functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class FNMath extends Fun {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNMath(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item atomic(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    if(def == FunDef.PI) return Dbl.get(Math.PI);
    if(expr[0].empty()) return null;

    final double d = checkDbl(expr[0], ctx);
    switch(def) {
      case SQRT: return Dbl.get(Math.sqrt(d));
      case SIN:  return Dbl.get(Math.sin(d));
      case COS:  return Dbl.get(Math.cos(d));
      case TAN:  return Dbl.get(Math.tan(d));
      case ASIN: return Dbl.get(Math.asin(d));
      case ACOS: return Dbl.get(Math.acos(d));
      case ATAN: return Dbl.get(Math.atan(d));
      default:   return super.atomic(ctx, ii);
    }
  }

  @Override
  public boolean xquery11() {
    return true;
  }
}
