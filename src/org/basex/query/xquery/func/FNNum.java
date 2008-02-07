package org.basex.query.xquery.func;

import java.math.BigDecimal;

import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.Dec;
import org.basex.query.xquery.item.Flt;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Itr;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;

/**
 * Numeric functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class FNNum extends Fun {
  @Override
  public Iter iter(final XQContext ctx, final Iter[] arg) throws XQException {
    final Iter iter = arg[0];
    final Item it = iter.atomic(this, true);
    if(it == null) return Iter.EMPTY;
    if(!it.u() && !it.n()) Err.num(info(), it);
    final double d = it.dbl();

    switch(func) {
      case ABS:    return abs(it);
      case CEIL:   return num(it, d, Math.ceil(d));
      case FLOOR:  return num(it, d, Math.floor(d));
      case RND:    return num(it, d, Math.floor(d + .5));
      case RNDHLF: return rnd(it, d, arg.length == 2 ? arg[1] : null);
      default:     throw new RuntimeException("Not defined: " + func);
    }
  }

  /**
   * Returns the absolute item.
   * @param it input item
   * @return absolute item
   * @throws XQException evaluation exception
   */
  private Iter abs(final Item it) throws XQException {
    final double d = it.dbl();
    final boolean s = d > 0d || 1 / d > 0;

    switch(it.type) {
      case DBL: return s ? it.iter() : Dbl.iter(Math.abs(it.dbl()));
      case FLT: return s ? it.iter() : Flt.iter(Math.abs(it.flt()));
      case DEC: return s ? it.iter() : Dec.iter(it.dec().abs());
      case ITR: return s ? it.iter() : Itr.iter(Math.abs(it.itr()));
      default:  return Itr.iter(Math.abs(it.itr()));
    }
  }

  /**
   * Returns a numeric result with the correct data type.
   * @param it input item
   * @param n input double value
   * @param d calculated double value
   * @return numeric item
   */
  private Iter num(final Item it, final double n, final double d) {
    final Item i = it.u() ? Dbl.get(n) : it;
    if(n == d) return i.iter();

    switch(it.type) {
      case DEC: return Dec.iter(d);
      case DBL: return Dbl.iter(d);
      case FLT: return Flt.iter((float) d);
      default:  return Itr.iter((long) d);
    }
  }

  /**
   * Returns a rounded item.
   * @param it input item
   * @param n input double value
   * @param pr precision
   * @return rounded item
   * @throws XQException evaluation exception
   */
  private Iter rnd(final Item it, final double n, final Iter pr)
      throws XQException {

    final int pp = pr == null ? 0 : (int) checkItr(pr);
    if(it.type == Type.DEC && pp >= 0) {
      return Dec.iter(it.dec().setScale(pp, BigDecimal.ROUND_HALF_EVEN));
    }

    final double p = p(pp);
    double d = n;
    if(p == 1 && (d % 2 == .5 || d % 2 == -1.5)) d -= .5;
    else d = Math.floor(d * p + .5) / p;
    return num(it, n, d);
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
