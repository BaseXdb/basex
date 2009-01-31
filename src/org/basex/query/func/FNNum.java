package org.basex.query.func;

import java.math.BigDecimal;
import org.basex.BaseX;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Dbl;
import org.basex.query.item.Dec;
import org.basex.query.item.Flt;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;

/**
 * Numeric functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class FNNum extends Fun {
  @Override
  public Iter iter(final QueryContext ctx, final Iter[] arg)
      throws QueryException {
    final Iter iter = arg[0];
    final Item it = iter.atomic(this, true);
    if(it == null) return Iter.EMPTY;
    return item(it, arg.length == 2 ? arg[1] : null).iter();
  }
  
  @Override
  public Expr c(final QueryContext ctx) throws QueryException {
    if(args[0].e()) return args[0];
    for(final Expr a : args) if(!a.i()) return this;

    final Item it = (Item) args[0];
    final Iter arg = args.length == 2 ? ((Item) args[1]).iter() : null;
    return item(it, arg);
  }

  /**
   * Evaluates the numeric function.
   * @param it input item
   * @param arg optional argument
   * @return result
   * @throws QueryException query exception
   */
  private Item item(final Item it, final Iter arg) throws QueryException {
    if(!it.u() && !it.n()) Err.num(info(), it);
    final double d = it.dbl();
    switch(func) {
      case ABS:    return abs(it);
      case CEIL:   return num(it, d, Math.ceil(d));
      case FLOOR:  return num(it, d, Math.floor(d));
      case RND:    return rnd(it);
      case RNDHLF: return rnd(it, d, arg);
      default: BaseX.notexpected(func); return null;
    }
  }

  /**
   * Returns the absolute item.
   * @param it input item
   * @return absolute item
   * @throws QueryException evaluation exception
   */
  private Item abs(final Item it) throws QueryException {
    final double d = it.dbl();
    final boolean s = d > 0d || 1 / d > 0;

    switch(it.type) {
      case DBL: return s ? it : Dbl.get(Math.abs(it.dbl()));
      case FLT: return s ? it : Flt.get(Math.abs(it.flt()));
      case DEC: return s ? it : Dec.get(it.dec().abs());
      case ITR: return s ? it : Itr.get(Math.abs(it.itr()));
      default:  return Itr.get(Math.abs(it.itr()));
    }
  }

  /**
   * Returns a rounded item.
   * @param it input item
   * @return absolute item
   * @throws QueryException evaluation exception
   */
  private Item rnd(final Item it) throws QueryException {
    final double d = it.dbl();
    if(it.type != Type.DEC) {
      return num(it, d, d == d && d != 0 && d >= Long.MIN_VALUE &&
          d < Long.MAX_VALUE ? d >= -.5d && d < 0 ? -0d : Math.round(d) : d);
    }
    final BigDecimal bd = it.dec();
    return Dec.get(bd.setScale(0, bd.signum() > 0 ? BigDecimal.ROUND_HALF_UP :
      BigDecimal.ROUND_HALF_DOWN));
  }

  /**
   * Returns a rounded item.
   * @param it input item
   * @param n input double value
   * @param pr precision
   * @return rounded item
   * @throws QueryException evaluation exception
   */
  private Item rnd(final Item it, final double n, final Iter pr)
      throws QueryException {

    final int pp = pr == null ? 0 : (int) checkItr(pr);
    if(it.type == Type.DEC && pp >= 0) {
      return Dec.get(it.dec().setScale(pp, BigDecimal.ROUND_HALF_EVEN));
    }

    final double p = p(pp);
    double d = n;
    if(p == 1 && (d % 2 == .5 || d % 2 == -1.5)) d -= .5;
    else d = Math.floor(d * p + .5) / p;
    return num(it, n, d);
  }

  /**
   * Returns a numeric result with the correct data type.
   * @param it input item
   * @param n input double value
   * @param d calculated double value
   * @return numeric item
   */
  private Item num(final Item it, final double n, final double d) {
    final Item i = it.u() ? Dbl.get(n) : it;
    if(n == d) return i;

    switch(it.type) {
      case DEC: return Dec.get(d);
      case DBL: return Dbl.get(d);
      case FLT: return Flt.get((float) d);
      default:  return Itr.get((long) d);
    }
  }

  /**
   * Returns the precision factor.
   * @param p precision
   * @return factor
   */
  private double p(final long p) {
    double f = 1;
    for(long i = p; i > 0; i--) f *= 10;
    for(long i = p; i < 0; i++) f /= 10;
    return f;
  }
}
