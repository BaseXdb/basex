package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.AtomType.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Calculation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public enum Calc {
  /** Addition. */
  PLUS("+") {
    @Override
    public Item ev(final InputInfo ii, final Item it1, final Item it2) throws QueryException {
      final Type t1 = it1.type, t2 = it2.type;
      final boolean n1 = t1.isNumberOrUntyped(), n2 = t2.isNumberOrUntyped();
      if(n1 ^ n2) throw numberError(ii, n1 ? it2 : it1);

      if(n1 && n2) {
        // numbers or untyped values
        final Type t = type(t1, t2);
        if(t == ITR) {
          final long l1 = it1.itr(ii), l2 = it2.itr(ii);
          if(l2 > 0 ? l1 > Long.MAX_VALUE - l2 : l1 < Long.MIN_VALUE - l2)
            throw RANGE_X.get(ii, l1 + " + " + l2);
          return Int.get(l1 + l2);
        }
        if(t == DBL) return Dbl.get(it1.dbl(ii) + it2.dbl(ii));
        if(t == FLT) return Flt.get(it1.flt(ii) + it2.flt(ii));
        return Dec.get(it1.dec(ii).add(it2.dec(ii)));
      }

      // dates or durations
      if(t1 == t2) {
        if(!(it1 instanceof Dur)) throw numberError(ii, n1 ? it2 : it1);
        if(t1 == YMD) return new YMDur((YMDur) it1, (YMDur) it2, true, ii);
        if(t1 == DTD) return new DTDur((DTDur) it1, (DTDur) it2, true, ii);
      }
      if(t1 == DTM) return new Dtm((Dtm) it1, checkDur(ii, it2), true, ii);
      if(t2 == DTM) return new Dtm((Dtm) it2, checkDur(ii, it1), true, ii);
      if(t1 == DAT) return new Dat((Dat) it1, checkDur(ii, it2), true, ii);
      if(t2 == DAT) return new Dat((Dat) it2, checkDur(ii, it1), true, ii);
      if(t1 == TIM && t2 == DTD) return new Tim((Tim) it1, (DTDur) it2, true);
      if(t2 == TIM && t1 == DTD) return new Tim((Tim) it2, (DTDur) it1, true);
      throw typeError(ii, t1, t2);
    }
  },

  /** Subtraction. */
  MINUS("-") {
    @Override
    public Item ev(final InputInfo ii, final Item it1, final Item it2) throws QueryException {
      final Type t1 = it1.type, t2 = it2.type;
      final boolean n1 = t1.isNumberOrUntyped(), n2 = t2.isNumberOrUntyped();
      if(n1 ^ n2) throw numberError(ii, n1 ? it2 : it1);

      if(n1 && n2) {
        // numbers or untyped values
        final Type t = type(t1, t2);
        if(t == ITR) {
          final long l1 = it1.itr(ii), l2 = it2.itr(ii);
          if(l2 < 0 ? l1 > Long.MAX_VALUE + l2 : l1 < Long.MIN_VALUE + l2)
            throw RANGE_X.get(ii, l1 + " - " + l2);
          return Int.get(l1 - l2);
        }
        if(t == DBL) return Dbl.get(it1.dbl(ii) - it2.dbl(ii));
        if(t == FLT) return Flt.get(it1.flt(ii) - it2.flt(ii));
        return Dec.get(it1.dec(ii).subtract(it2.dec(ii)));
      }

      // dates or durations
      if(t1 == t2) {
        if(t1 == DTM || t1 == DAT || t1 == TIM) return new DTDur((ADate) it1, (ADate) it2, ii);
        if(t1 == YMD) return new YMDur((YMDur) it1, (YMDur) it2, false, ii);
        if(t1 == DTD) return new DTDur((DTDur) it1, (DTDur) it2, false, ii);
        throw numberError(ii, n1 ? it2 : it1);
      }
      if(t1 == DTM) return new Dtm((Dtm) it1, checkDur(ii, it2), false, ii);
      if(t1 == DAT) return new Dat((Dat) it1, checkDur(ii, it2), false, ii);
      if(t1 == TIM && t2 == DTD) return new Tim((Tim) it1, (DTDur) it2, false);
      throw typeError(ii, t1, t2);
    }
  },

  /** Multiplication. */
  MULT("*") {
    @Override
    public Item ev(final InputInfo ii, final Item it1, final Item it2) throws QueryException {
      final Type t1 = it1.type, t2 = it2.type;
      if(t1 == YMD) {
        if(it2 instanceof ANum) return new YMDur((Dur) it1, it2.dbl(ii), true, ii);
        throw numberError(ii, it2);
      }
      if(t2 == YMD) {
        if(it1 instanceof ANum) return new YMDur((Dur) it2, it1.dbl(ii), true, ii);
        throw numberError(ii, it1);
      }
      if(t1 == DTD) {
        if(it2 instanceof ANum) return new DTDur((Dur) it1, it2.dbl(ii), true, ii);
        throw numberError(ii, it2);
      }
      if(t2 == DTD) {
        if(it1 instanceof ANum) return new DTDur((Dur) it2, it1.dbl(ii), true, ii);
        throw numberError(ii, it1);
      }

      final boolean b1 = t1.isNumberOrUntyped(), b2 = t2.isNumberOrUntyped();
      if(b1 ^ b2) throw typeError(ii, t1, t2);
      if(b1 && b2) {
        final Type t = type(t1, t2);
        if(t == ITR) {
          final long l1 = it1.itr(ii);
          final long l2 = it2.itr(ii);
          if(l2 > 0 ? l1 > Long.MAX_VALUE / l2 || l1 < Long.MIN_VALUE / l2
                    : l2 < -1 ? l1 > Long.MIN_VALUE / l2 || l1 < Long.MAX_VALUE / l2
                              : l2 == -1 && l1 == Long.MIN_VALUE)
            throw RANGE_X.get(ii, l1 + " * " + l2);
          return Int.get(l1 * l2);
        }
        if(t == DBL) return Dbl.get(it1.dbl(ii) * it2.dbl(ii));
        if(t == FLT) return Flt.get(it1.flt(ii) * it2.flt(ii));
        return Dec.get(it1.dec(ii).multiply(it2.dec(ii)));
      }
      throw numberError(ii, b1 ? it2 : it1);
    }
  },

  /** Division. */
  DIV("div") {
    @Override
    public Item ev(final InputInfo ii, final Item it1, final Item it2) throws QueryException {
      final Type t1 = it1.type, t2 = it2.type;
      if(t1 == t2) {
        if(t1 == YMD) {
          final BigDecimal bd = BigDecimal.valueOf(((YMDur) it2).ymd());
          if(bd.doubleValue() == .0) throw zeroError(ii, it1);
          return Dec.get(BigDecimal.valueOf(((YMDur) it1).ymd()).divide(
              bd, 20, RoundingMode.HALF_EVEN));
        }
        if(t1 == DTD) {
          final BigDecimal bd = ((DTDur) it2).dtd();
          if(bd.doubleValue() == .0) throw zeroError(ii, it1);
          return Dec.get(((DTDur) it1).dtd().divide(bd, 20, RoundingMode.HALF_EVEN));
        }
      }
      if(t1 == YMD) {
        if(it2 instanceof ANum) return new YMDur((Dur) it1, it2.dbl(ii), false, ii);
        throw numberError(ii, it2);
      }
      if(t1 == DTD) {
        if(it2 instanceof ANum) return new DTDur((Dur) it1, it2.dbl(ii), false, ii);
        throw numberError(ii, it2);
      }

      checkNum(ii, it1, it2);
      final Type t = type(t1, t2);
      if(t == DBL) return Dbl.get(it1.dbl(ii) / it2.dbl(ii));
      if(t == FLT) return Flt.get(it1.flt(ii) / it2.flt(ii));

      final BigDecimal b1 = it1.dec(ii);
      final BigDecimal b2 = it2.dec(ii);
      if(b2.signum() == 0) throw zeroError(ii, it1);
      final int s = Math.max(18, Math.max(b1.scale(), b2.scale()));
      return Dec.get(b1.divide(b2, s, RoundingMode.HALF_EVEN));
    }
  },

  /** Integer division. */
  IDIV("idiv") {
    @Override
    public Item ev(final InputInfo ii, final Item it1, final Item ti2) throws QueryException {
      checkNum(ii, it1, ti2);
      final Type t = type(it1.type, ti2.type);
      if(t == DBL || t == FLT) {
        final double d1 = it1.dbl(ii), d2 = ti2.dbl(ii);
        if(d2 == 0) throw zeroError(ii, it1);
        final double d = d1 / d2;
        if(Double.isNaN(d) || Double.isInfinite(d)) throw DIVFLOW_X.get(ii, d1 + " idiv " + d2);
        if(d < Long.MIN_VALUE || d > Long.MAX_VALUE) throw RANGE_X.get(ii, d1 + " idiv " + d2);
        return Int.get((long) d);
      }

      if(t == ITR) {
        final long b1 = it1.itr(ii), b2 = ti2.itr(ii);
        if(b2 == 0) throw zeroError(ii, it1);
        if(b1 == Integer.MIN_VALUE && b2 == -1) throw RANGE_X.get(ii, b1 + " idiv " + b2);
        return Int.get(b1 / b2);
      }

      final BigDecimal b1 = it1.dec(ii), b2 = ti2.dec(ii);
      if(b2.signum() == 0) throw zeroError(ii, it1);
      final BigDecimal res = b1.divideToIntegralValue(b2);
      if(!(MIN_LONG.compareTo(res) <= 0 && res.compareTo(MAX_LONG) <= 0))
        throw RANGE_X.get(ii, b1 + " idiv " + b2);
      return Int.get(res.longValueExact());
    }
  },

  /** Modulo. */
  MOD("mod") {
    @Override
    public Item ev(final InputInfo ii, final Item it1, final Item it2) throws QueryException {
      checkNum(ii, it1, it2);
      final Type t = type(it1.type, it2.type);
      if(t == DBL) return Dbl.get(it1.dbl(ii) % it2.dbl(ii));
      if(t == FLT) return Flt.get(it1.flt(ii) % it2.flt(ii));
      if(t == ITR) {
        final long b1 = it1.itr(ii), b2 = it2.itr(ii);
        if(b2 == 0) throw zeroError(ii, it1);
        return Int.get(b1 % b2);
      }

      final BigDecimal b1 = it1.dec(ii), b2 = it2.dec(ii);
      if(b2.signum() == 0) throw zeroError(ii, it1);
      final BigDecimal q = b1.divide(b2, 0, RoundingMode.DOWN);
      return Dec.get(b1.subtract(q.multiply(b2)));
    }
  };

  /** {@link Long#MIN_VALUE} as a {@link BigDecimal}. */
  private static final BigDecimal MIN_LONG = BigDecimal.valueOf(Long.MIN_VALUE);
  /** {@link Long#MAX_VALUE} as a {@link BigDecimal}. */
  private static final BigDecimal MAX_LONG = BigDecimal.valueOf(Long.MAX_VALUE);

  /** Name of operation. */
  final String name;

  /**
   * Constructor.
   * @param name name
   */
  Calc(final String name) {
    this.name = name;
  }

  /**
   * Performs the calculation.
   * @param ii input info
   * @param it1 first item
   * @param it2 second item
   * @return result type
   * @throws QueryException query exception
   */
  public abstract Item ev(final InputInfo ii, final Item it1, final Item it2) throws QueryException;

  /**
   * Returns the numeric type with the highest precedence.
   * @param t1 first item type
   * @param t2 second item type
   * @return type
   */
  public static Type type(final Type t1, final Type t2) {
    if(t1 == DBL || t2 == DBL || t1.isUntyped() || t2.isUntyped()) return DBL;
    if(t1 == FLT || t2 == FLT) return FLT;
    if(t1 == DEC || t2 == DEC) return DEC;
    return ITR;
  }

  /**
   * Throws a division by zero exception.
   * @param ii input info
   * @param it item
   * @return query exception (indicates that an error is raised)
   * @throws QueryException query exception
   */
  private static QueryException zeroError(final InputInfo ii, final Item it) throws QueryException {
    return DIVZERO_X.get(ii, chop(it, ii));
  }

  /**
   * Returns a type error.
   * @param ii input info
   * @param t1 first type
   * @param t2 second type
   * @return query exception
   */
  final QueryException typeError(final InputInfo ii, final Type t1, final Type t2) {
    return CALCTYPE_X_X_X.get(ii, info(), t1, t2);
  }

  /**
   * Returns a duration type error.
   * @param ii input info
   * @param it item
   * @return duration
   * @throws QueryException query exception
   */
  static Dur checkDur(final InputInfo ii, final Item it) throws QueryException {
    final Type ip = it.type;
    if(!(it instanceof Dur)) throw NODUR_X_X.get(ii, ip, it);
    if(ip == DUR) throw NOSUBDUR_X.get(ii, it);
    return (Dur) it;
  }

  /**
   * Checks if the specified items are numeric or untyped.
   * @param ii input info
   * @param it1 first item
   * @param it2 second item
   * @throws QueryException query exception
   */
  static void checkNum(final InputInfo ii, final Item it1, final Item it2)
      throws QueryException {
    if(!it1.type.isNumberOrUntyped()) throw numberError(ii, it1);
    if(!it2.type.isNumberOrUntyped()) throw numberError(ii, it2);
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
