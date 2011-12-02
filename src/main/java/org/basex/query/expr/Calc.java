package org.basex.query.expr;

import static org.basex.query.item.AtomType.*;
import java.math.BigDecimal;
import org.basex.query.QueryException;
import org.basex.query.item.DTd;
import org.basex.query.item.Dat;
import org.basex.query.item.Date;
import org.basex.query.item.Dbl;
import org.basex.query.item.Dec;
import org.basex.query.item.Dtm;
import org.basex.query.item.Dur;
import org.basex.query.item.Flt;
import org.basex.query.item.Item;
import org.basex.query.item.Int;
import org.basex.query.item.Tim;
import org.basex.query.item.Type;
import org.basex.query.item.YMd;
import org.basex.query.util.Err;
import static org.basex.query.util.Err.*;
import org.basex.util.InputInfo;

/**
 * Calculation.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public enum Calc {
  /** Addition. */
  PLUS("+") {
    @Override
    public Item ev(final InputInfo ii, final Item a, final Item b)
        throws QueryException {

      final Type ta = a.type, tb = b.type;
      final boolean t1 = ta.isNumber() || ta.isUntyped();
      final boolean t2 = tb.isNumber() || tb.isUntyped();
      if(t1 ^ t2) errNum(ii, !t1 ? a : b);
      if(t1 && t2) {
        final Type t = type(ta, tb);
        if(t == ITR) {
          final long l1 = a.itr(ii);
          final long l2 = b.itr(ii);
          checkRange(ii, l1 + (double) l2);
          return Int.get(l1 + l2);
        }
        if(t == DBL) return Dbl.get(a.dbl(ii) + b.dbl(ii));
        if(t == FLT) return Flt.get(a.flt(ii) + b.flt(ii));
        return Dec.get(a.dec(ii).add(b.dec(ii)));
      }

      if(ta == tb) {
        if(!ta.isDuration()) errNum(ii, !t1 ? a : b);
        if(ta == YMD) return new YMd((YMd) a, (YMd) b, true);
        if(ta == DTD) return new DTd((DTd) a, (DTd) b, true);
      }
      if(ta == DTM) return new Dtm((Date) a, checkDur(ii, b), true, ii);
      if(tb == DTM) return new Dtm((Date) b, checkDur(ii, a), true, ii);
      if(ta == DAT) return new Dat((Date) a, checkDur(ii, b), true, ii);
      if(tb == DAT) return new Dat((Date) b, checkDur(ii, a), true, ii);
      if(ta == TIM) {
        if(tb != DTD) errType(ii, DTD, b);
        return new Tim((Tim) a, (DTd) b, true, ii);
      }
      if(tb == TIM) {
        if(ta != DTD) errType(ii, DTD, b);
        return new Tim((Tim) b, (DTd) a, true, ii);
      }
      errType(ii, ta, b);
      return null;
    }
  },

  /** Subtraction. */
  MINUS("-") {
    @Override
    public Item ev(final InputInfo ii, final Item a, final Item b)
        throws QueryException {

      final Type ta = a.type, tb = b.type;
      final boolean t1 = ta.isNumber() || ta.isUntyped();
      final boolean t2 = tb.isNumber() || tb.isUntyped();
      if(t1 ^ t2) errNum(ii, !t1 ? a : b);
      if(t1 && t2) {
        final Type t = type(ta, tb);
        if(t == ITR) {
          final long l1 = a.itr(ii);
          final long l2 = b.itr(ii);
          checkRange(ii, l1 - (double) l2);
          return Int.get(l1 - l2);
        }
        if(t == DBL) return Dbl.get(a.dbl(ii) - b.dbl(ii));
        if(t == FLT) return Flt.get(a.flt(ii) - b.flt(ii));
        return Dec.get(a.dec(ii).subtract(b.dec(ii)));
      }

      if(ta == tb) {
        if(ta == DTM || ta == DAT || ta == TIM)
          return new DTd((Date) a, (Date) b);
        if(ta == YMD) return new YMd((YMd) a, (YMd) b, false);
        if(ta == DTD) return new DTd((DTd) a, (DTd) b, false);
        errNum(ii, !t1 ? a : b);
      }
      if(ta == DTM) return new Dtm((Date) a, checkDur(ii, b), false, ii);
      if(ta == DAT) return new Dat((Date) a, checkDur(ii, b), false, ii);
      if(ta == TIM) {
        if(tb != DTD) errType(ii, DTD, b);
        return new Tim((Tim) a, (DTd) b, false, ii);
      }
      errType(ii, ta, b);
      return null;
    }
  },

  /** Multiplication. */
  MULT("*") {
    @Override
    public Item ev(final InputInfo ii, final Item a, final Item b)
        throws QueryException {

      final Type ta = a.type, tb = b.type;
      if(ta == YMD) {
        if(!tb.isNumber()) errNum(ii, b);
        return new YMd((Dur) a, b.dbl(ii), true, ii);
      }
      if(tb == YMD) {
        if(!ta.isNumber()) errNum(ii, a);
        return new YMd((Dur) b, a.dbl(ii), true, ii);
      }
      if(ta == DTD) {
        if(!tb.isNumber()) errNum(ii, b);
        return new DTd((Dur) a, b.dbl(ii), true, ii);
      }
      if(tb == DTD) {
        if(!ta.isNumber()) errNum(ii, a);
        return new DTd((Dur) b, a.dbl(ii), true, ii);
      }

      final boolean t1 = ta.isNumber() || ta.isUntyped();
      final boolean t2 = tb.isNumber() || tb.isUntyped();
      if(t1 ^ t2) errType(ii, ta, b);
      if(t1 && t2) {
        final Type t = type(ta, tb);
        if(t == ITR) {
          final long l1 = a.itr(ii);
          final long l2 = b.itr(ii);
          checkRange(ii, l1 * (double) l2);
          return Int.get(l1 * l2);
        }
        if(t == DBL) return Dbl.get(a.dbl(ii) * b.dbl(ii));
        if(t == FLT) return Flt.get(a.flt(ii) * b.flt(ii));
        return Dec.get(a.dec(ii).multiply(b.dec(ii)));
      }
      errNum(ii, !t1 ? a : b);
      return null;
    }
  },

  /** Division. */
  DIV("div") {
    @Override
    public Item ev(final InputInfo ii, final Item a, final Item b)
        throws QueryException {

      final Type ta = a.type, tb = b.type;
      if(ta == tb) {
        if(ta == YMD) {
          final BigDecimal bd = BigDecimal.valueOf(((YMd) b).ymd());
          if(bd.equals(BigDecimal.ZERO)) DATEZERO.thrw(ii, info());
          return Dec.get(BigDecimal.valueOf(((YMd) a).ymd()).divide(
              bd, 20, BigDecimal.ROUND_HALF_EVEN));
        }
        if(ta == DTD) {
          final BigDecimal bd = ((DTd) b).dtd();
          if(bd.equals(BigDecimal.ZERO)) DATEZERO.thrw(ii, info());
          return Dec.get(((DTd) a).dtd().divide(bd, 20,
              BigDecimal.ROUND_HALF_EVEN));
        }
      }
      if(ta == YMD) {
        if(!tb.isNumber()) errNum(ii, b);
        return new YMd((Dur) a, b.dbl(ii), false, ii);
      }
      if(ta == DTD) {
        if(!tb.isNumber()) errNum(ii, b);
        return new DTd((Dur) a, b.dbl(ii), false, ii);
      }

      checkNum(ii, a, b);
      final Type t = type(ta, tb);
      if(t == DBL) return Dbl.get(a.dbl(ii) / b.dbl(ii));
      if(t == FLT) return Flt.get(a.flt(ii) / b.flt(ii));

      final BigDecimal b1 = a.dec(ii);
      final BigDecimal b2 = b.dec(ii);
      if(b2.signum() == 0) DIVZERO.thrw(ii, a);
      final int s = Math.max(18, Math.max(b1.scale(), b2.scale()));
      return Dec.get(b1.divide(b2, s, BigDecimal.ROUND_HALF_EVEN));
    }
  },

  /** Integer division. */
  IDIV("idiv") {
    @Override
    public Item ev(final InputInfo ii, final Item a, final Item b)
        throws QueryException {

      checkNum(ii, a, b);
      final double d1 = a.dbl(ii);
      final double d2 = b.dbl(ii);
      if(d2 == 0) DIVZERO.thrw(ii, a);
      final double d = d1 / d2;
      if(Double.isNaN(d) || Double.isInfinite(d)) DIVFLOW.thrw(ii, d1, d2);
      return Int.get(type(a.type, b.type) == ITR ?
          a.itr(ii) / b.itr(ii) : (long) d);
    }
  },

  /** Modulo. */
  MOD("mod") {
    @Override
    public Item ev(final InputInfo ii, final Item a, final Item b)
        throws QueryException {

      checkNum(ii, a, b);
      final Type t = type(a.type, b.type);
      if(t == DBL) return Dbl.get(a.dbl(ii) % b.dbl(ii));
      if(t == FLT) return Flt.get(a.flt(ii) % b.flt(ii));
      if(t == ITR) {
        final long b1 = a.itr(ii);
        final long b2 = b.itr(ii);
        if(b2 == 0) DIVZERO.thrw(ii, a);
        return Int.get(b1 % b2);
      }

      final BigDecimal b1 = a.dec(ii);
      final BigDecimal b2 = b.dec(ii);
      if(b2.signum() == 0) DIVZERO.thrw(ii, a);
      final BigDecimal q = b1.divide(b2, 0, BigDecimal.ROUND_DOWN);
      return Dec.get(b1.subtract(q.multiply(b2)));
    }
  };

  /** Name of operation. */
  final String name;

  /**
   * Constructor.
   * @param n name
   */
  private Calc(final String n) {
    name = n;
  }

  /**
   * Performs the calculation.
   * @param ii input info
   * @param a first item
   * @param b second item
   * @return result type
   * @throws QueryException query exception
   */
  public abstract Item ev(final InputInfo ii, final Item a, final Item b)
    throws QueryException;

  /**
   * Returns the numeric type with the highest precedence.
   * @param a first item type
   * @param b second item type
   * @return type
   */
  static final Type type(final Type a, final Type b) {
    if(a == DBL || b == DBL || a.isUntyped() || b.isUntyped()) return DBL;
    if(a == FLT || b == FLT) return FLT;
    if(a == DEC || b == DEC) return DEC;
    return ITR;
  }

  /**
   * Returns a type error.
   * @param ii input info
   * @param t expected type
   * @param it item
   * @throws QueryException query exception
   */
  final void errType(final InputInfo ii, final Type t, final Item it)
      throws QueryException {
    Err.type(ii, info(), t, it);
  }

  /**
   * Returns a numeric type error.
   * @param ii input info
   * @param it item
   * @throws QueryException query exception
   */
  final void errNum(final InputInfo ii, final Item it) throws QueryException {
    XPTYPENUM.thrw(ii, info(), it.type);
  }

  /**
   * Returns a duration type error.
   * @param ii input info
   * @param it item
   * @return duration
   * @throws QueryException query exception
   */
  final Dur checkDur(final InputInfo ii, final Item it) throws QueryException {
    final Type ip = it.type;
    if(!ip.isDuration()) XPDUR.thrw(ii, info(), ip);
    if(ip == DUR) throw SIMPLDUR.thrw(ii, info(), it);
    return (Dur) it;
  }

  /**
   * Checks if the specified items are numeric or untyped.
   * @param ii input info
   * @param a first item
   * @param b second item
   * @throws QueryException query exception
   */
  final void checkNum(final InputInfo ii, final Item a, final Item b)
      throws QueryException {
    final Type ta = a.type;
    final Type tb = b.type;
    if(!ta.isUntyped() && !ta.isNumber()) errNum(ii, a);
    if(!tb.isUntyped() && !tb.isNumber()) errNum(ii, b);
  }

  /**
   * Checks if the specified value is outside the integer range.
   * @param ii input info
   * @param d value to be checked
   * @throws QueryException query exception
   */
  final void checkRange(final InputInfo ii, final double d)
      throws QueryException {
    if(d < Long.MIN_VALUE || d > Long.MAX_VALUE) RANGE.thrw(ii, d);
  }

  /**
   * Returns a string representation of the operator.
   * @return string
   */
  final String info() {
    return "'" + name + "' operator";
  }

  @Override
  public String toString() {
    return name;
  }
}
