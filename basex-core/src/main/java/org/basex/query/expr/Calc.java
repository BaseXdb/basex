package org.basex.query.expr;

import static org.basex.query.util.Err.*;
import static org.basex.query.value.type.AtomType.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.item.ANum;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Calculation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public enum Calc {
  /** Addition. */
  PLUS("+") {
    @Override
    public Item ev(final InputInfo ii, final Item a, final Item b) throws QueryException {
      final Type ta = a.type, tb = b.type;
      final boolean t1 = ta.isNumberOrUntyped();
      final boolean t2 = tb.isNumberOrUntyped();
      if(t1 ^ t2) throw numError(ii, t1 ? b : a);

      if(t1 && t2) {
        // numbers or untyped values
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

      // dates or durations
      if(ta == tb) {
        if(!(a instanceof Dur)) throw numError(ii, t1 ? b : a);
        if(ta == YMD) return new YMDur((YMDur) a, (YMDur) b, true, ii);
        if(ta == DTD) return new DTDur((DTDur) a, (DTDur) b, true, ii);
      }
      if(ta == DTM) return new Dtm((Dtm) a, checkDur(ii, b), true, ii);
      if(tb == DTM) return new Dtm((Dtm) b, checkDur(ii, a), true, ii);
      if(ta == DAT) return new Dat((Dat) a, checkDur(ii, b), true, ii);
      if(tb == DAT) return new Dat((Dat) b, checkDur(ii, a), true, ii);
      if(ta == TIM && tb == DTD) return new Tim((Tim) a, (DTDur) b, true);
      if(tb == TIM && ta == DTD) return new Tim((Tim) b, (DTDur) a, true);
      throw typeError(ii, ta, tb);
    }
  },

  /** Subtraction. */
  MINUS("-") {
    @Override
    public Item ev(final InputInfo ii, final Item a, final Item b) throws QueryException {
      final Type ta = a.type, tb = b.type;
      final boolean t1 = ta.isNumberOrUntyped();
      final boolean t2 = tb.isNumberOrUntyped();
      if(t1 ^ t2) throw numError(ii, t1 ? b : a);

      if(t1 && t2) {
        // numbers or untyped values
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

      // dates or durations
      if(ta == tb) {
        if(ta == DTM || ta == DAT || ta == TIM)
          return new DTDur((ADate) a, (ADate) b, ii);
        if(ta == YMD) return new YMDur((YMDur) a, (YMDur) b, false, ii);
        if(ta == DTD) return new DTDur((DTDur) a, (DTDur) b, false, ii);
        throw numError(ii, t1 ? b : a);
      }
      if(ta == DTM) return new Dtm((Dtm) a, checkDur(ii, b), false, ii);
      if(ta == DAT) return new Dat((Dat) a, checkDur(ii, b), false, ii);
      if(ta == TIM && tb == DTD) return new Tim((Tim) a, (DTDur) b, false);
      throw typeError(ii, ta, tb);
    }
  },

  /** Multiplication. */
  MULT("*") {
    @Override
    public Item ev(final InputInfo ii, final Item a, final Item b) throws QueryException {
      final Type ta = a.type, tb = b.type;
      if(ta == YMD) {
        if(b instanceof ANum) return new YMDur((Dur) a, b.dbl(ii), true, ii);
        throw numError(ii, b);
      }
      if(tb == YMD) {
        if(a instanceof ANum) return new YMDur((Dur) b, a.dbl(ii), true, ii);
        throw numError(ii, a);
      }
      if(ta == DTD) {
        if(b instanceof ANum) return new DTDur((Dur) a, b.dbl(ii), true, ii);
        throw numError(ii, b);
      }
      if(tb == DTD) {
        if(a instanceof ANum) return new DTDur((Dur) b, a.dbl(ii), true, ii);
        throw numError(ii, a);
      }

      final boolean t1 = ta.isNumberOrUntyped();
      final boolean t2 = tb.isNumberOrUntyped();
      if(t1 ^ t2) throw typeError(ii, ta, tb);
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
      throw numError(ii, t1 ? b : a);
    }
  },

  /** Division. */
  DIV("div") {
    @Override
    public Item ev(final InputInfo ii, final Item a, final Item b) throws QueryException {
      final Type ta = a.type, tb = b.type;
      if(ta == tb) {
        if(ta == YMD) {
          final BigDecimal bd = BigDecimal.valueOf(((YMDur) b).ymd());
          if(bd.doubleValue() == 0.0) throw DIVZERO.get(ii, chop(a));
          return Dec.get(BigDecimal.valueOf(((YMDur) a).ymd()).divide(
              bd, 20, BigDecimal.ROUND_HALF_EVEN));
        }
        if(ta == DTD) {
          final BigDecimal bd = ((DTDur) b).dtd();
          if(bd.doubleValue() == 0.0) throw DIVZERO.get(ii, chop(a));
          return Dec.get(((DTDur) a).dtd().divide(bd, 20,
              BigDecimal.ROUND_HALF_EVEN));
        }
      }
      if(ta == YMD) {
        if(b instanceof ANum) return new YMDur((Dur) a, b.dbl(ii), false, ii);
        throw numError(ii, b);
      }
      if(ta == DTD) {
        if(b instanceof ANum) return new DTDur((Dur) a, b.dbl(ii), false, ii);
        throw numError(ii, b);
      }

      checkNum(ii, a, b);
      final Type t = type(ta, tb);
      if(t == DBL) return Dbl.get(a.dbl(ii) / b.dbl(ii));
      if(t == FLT) return Flt.get(a.flt(ii) / b.flt(ii));

      final BigDecimal b1 = a.dec(ii);
      final BigDecimal b2 = b.dec(ii);
      if(b2.signum() == 0) throw DIVZERO.get(ii, chop(a));
      final int s = Math.max(18, Math.max(b1.scale(), b2.scale()));
      return Dec.get(b1.divide(b2, s, BigDecimal.ROUND_HALF_EVEN));
    }
  },

  /** Integer division. */
  IDIV("idiv") {
    @Override
    public Item ev(final InputInfo ii, final Item a, final Item b) throws QueryException {
      checkNum(ii, a, b);
      final double d1 = a.dbl(ii);
      final double d2 = b.dbl(ii);
      if(d2 == 0) throw DIVZERO.get(ii, chop(a));
      final double d = d1 / d2;
      if(Double.isNaN(d) || Double.isInfinite(d)) throw DIVFLOW.get(ii, d1, d2);
      checkRange(ii, d);
      return Int.get(type(a.type, b.type) == ITR ? a.itr(ii) / b.itr(ii) : (long) d);
    }
  },

  /** Modulo. */
  MOD("mod") {
    @Override
    public Item ev(final InputInfo ii, final Item a, final Item b) throws QueryException {
      checkNum(ii, a, b);
      final Type t = type(a.type, b.type);
      if(t == DBL) return Dbl.get(a.dbl(ii) % b.dbl(ii));
      if(t == FLT) return Flt.get(a.flt(ii) % b.flt(ii));
      if(t == ITR) {
        final long b1 = a.itr(ii);
        final long b2 = b.itr(ii);
        if(b2 == 0) throw DIVZERO.get(ii, chop(a));
        return Int.get(b1 % b2);
      }

      final BigDecimal b1 = a.dec(ii);
      final BigDecimal b2 = b.dec(ii);
      if(b2.signum() == 0) throw DIVZERO.get(ii, chop(a));
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
  Calc(final String n) {
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
  public abstract Item ev(final InputInfo ii, final Item a, final Item b) throws QueryException;

  /**
   * Returns the numeric type with the highest precedence.
   * @param a first item type
   * @param b second item type
   * @return type
   */
  public static Type type(final Type a, final Type b) {
    if(a == DBL || b == DBL || a.isUntyped() || b.isUntyped()) return DBL;
    if(a == FLT || b == FLT) return FLT;
    if(a == DEC || b == DEC) return DEC;
    return ITR;
  }

  /**
   * Returns a type error.
   * @param ii input info
   * @param ta first type
   * @param tb second type
   * @return query exception
   */
  final QueryException typeError(final InputInfo ii, final Type ta, final Type tb) {
    return CALCTYPE.get(ii, info(), ta, tb);
  }

  /**
   * Returns a numeric type error.
   * @param ii input info
   * @param it item
   * @return query exception
   */
  final QueryException numError(final InputInfo ii, final Item it) {
    return NONUMBER.get(ii, info(), it.type);
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
    if(!(it instanceof Dur)) throw NODUR.get(ii, info(), ip);
    if(ip == DUR) throw NOSUBDUR.get(ii, info(), it);
    return (Dur) it;
  }

  /**
   * Checks if the specified items are numeric or untyped.
   * @param ii input info
   * @param a first item
   * @param b second item
   * @throws QueryException query exception
   */
  final void checkNum(final InputInfo ii, final Item a, final Item b) throws QueryException {
    if(!a.type.isNumberOrUntyped()) throw numError(ii, a);
    if(!b.type.isNumberOrUntyped()) throw numError(ii, b);
  }

  /**
   * Checks if the specified value is outside the integer range.
   * @param ii input info
   * @param d value to be checked
   * @throws QueryException query exception
   */
  private static void checkRange(final InputInfo ii, final double d) throws QueryException {
    if(d < Long.MIN_VALUE || d > Long.MAX_VALUE) throw RANGE.get(ii, d);
  }

  /**
   * Returns a string representation of the operator.
   * @return string
   */
  final String info() {
    return '\'' + name + "' operator";
  }

  @Override
  public String toString() {
    return name;
  }
}
