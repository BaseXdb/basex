package org.basex.query.func;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;

/**
 * Math functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class FNMath extends Fun {
  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    if(func == FunDef.PI) return Dbl.get(3.141592653589793e0);
    if(expr[0].e()) return null;
    
    final double d = checkDbl(expr[0], ctx);
    switch(func) {
      case SQRT: return Dbl.get(Math.sqrt(d));
      case SIN:  return Dbl.get(Math.sin(d));
      case COS:  return Dbl.get(Math.cos(d));
      case TAN:  return Dbl.get(Math.tan(d));
      case ASIN: return Dbl.get(Math.asin(d));
      case ACOS: return Dbl.get(Math.acos(d));
      case ATAN: return Dbl.get(Math.atan(d));
      default:   return super.atomic(ctx);
    }
  }
}
