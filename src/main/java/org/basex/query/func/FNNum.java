package org.basex.query.func;

import java.math.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.item.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * Numeric functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNNum extends StandardFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNNum(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Item it = expr[0].item(ctx, info);
    if(it == null) return null;

    final Type ip = it.type;
    if(!ip.isUntyped() && !ip.isNumber()) Err.number(this, it);
    final double d = it.dbl(info);
    switch(sig) {
      case ABS:                return abs(it, info);
      case CEILING:            return num(it, d, StrictMath.ceil(d));
      case FLOOR:              return num(it, d, StrictMath.floor(d));
      case ROUND:              return rnd(it, d, false, ctx);
      case ROUND_HALF_TO_EVEN: return rnd(it, d, true, ctx);
      default:                 return super.item(ctx, ii);
    }
  }

  /**
   * Returns a rounded item.
   * @param it input item
   * @param d input double value
   * @param h2e half-to-even flag
   * @param ctx query context
   * @return absolute item
   * @throws QueryException query exception
   */
  private Item rnd(final Item it, final double d, final boolean h2e,
      final QueryContext ctx) throws QueryException {

    final int p = expr.length == 1 ? 0 : (int) checkItr(expr[1], ctx);
    return round(it, d, p, h2e, info);
  }

  /**
   * Returns an absolute number.
   * @param it input item
   * @param ii input info
   * @return absolute item
   * @throws QueryException query exception
   */
  private static Item abs(final Item it, final InputInfo ii) throws QueryException {
    final double d = it.dbl(ii);
    final boolean s = d > 0d || 1 / d > 0;

    final Type ip = it.type;
    if(ip instanceof AtomType) {
      switch((AtomType) ip) {
        case DBL: return s ? it : Dbl.get(Math.abs(it.dbl(ii)));
        case FLT: return s ? it : Flt.get(Math.abs((float) it.dbl(ii)));
        case DEC: return s ? it : Dec.get(it.dec(ii).abs());
        case ITR: return s ? it : Int.get(Math.abs(it.itr(ii)));
        default:  break;
      }
    }
    return ip.instanceOf(AtomType.ITR) ?
        Int.get(Math.abs(it.itr(ii))) : Dec.get(it.dec(ii).abs());
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
  public static Item round(final Item it, final double d, final int prec,
      final boolean h2e, final InputInfo ii) throws QueryException {

    // take care of untyped items
    final Item num = it.type.isUntyped() ? Dbl.get(it.dbl(ii)) : it;

    if(num.type == AtomType.DEC && prec >= 0) {
      final BigDecimal bd = num.dec(ii);
      final int m = h2e ? BigDecimal.ROUND_HALF_EVEN : bd.signum() > 0 ?
          BigDecimal.ROUND_HALF_UP : BigDecimal.ROUND_HALF_DOWN;
      return Dec.get(bd.setScale(prec, m));
    }

    // calculate precision factor
    double p = 1;
    for(long i = prec; i > 0; --i) p *= 10;
    for(long i = prec; i < 0; ++i) p /= 10;

    double c = d;
    if(!Double.isNaN(c) && !Double.isInfinite(c)) {
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
   * Returns a numeric result with the correct data type.
   * @param it input item
   * @param n input double value
   * @param d calculated double value
   * @return numeric item
   */
  private static Item num(final Item it, final double n, final double d) {
    final Type ip = it.type;
    final Item i = ip.isUntyped() ? Dbl.get(n) : it;
    if(n == d) return i;

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

  @Override
  public boolean uses(final Use u) {
    return u == Use.X30 && sig == Function.ROUND && expr.length == 2 ||
      super.uses(u);
  }
}
