package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.AtomType.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Calculation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public enum Calc {
  /** Addition. */
  PLUS("+") {
    @Override
    public Item eval(final Item item1, final Item item2, final InputInfo ii) throws QueryException {
      final Type type1 = item1.type, type2 = item2.type;
      final boolean num1 = type1.isNumberOrUntyped(), num2 = type2.isNumberOrUntyped();
      if(num1 ^ num2) throw numberError(num1 ? item2 : item1, ii);

      if(num1) {
        // numbers or untyped values
        final Type type = numType(type1, type2);
        if(type == INTEGER) {
          final long itr1 = item1.itr(ii), itr2 = item2.itr(ii);
          if(itr2 > 0 ? itr1 > Long.MAX_VALUE - itr2 : itr1 < Long.MIN_VALUE - itr2)
            throw RANGE_X.get(ii, itr1 + " + " + itr2);
          return Int.get(itr1 + itr2);
        }
        if(type == DOUBLE) return Dbl.get(item1.dbl(ii) + item2.dbl(ii));
        if(type == FLOAT) return Flt.get(item1.flt(ii) + item2.flt(ii));
        return Dec.get(item1.dec(ii).add(item2.dec(ii)));
      }

      // dates or durations
      if(type1 == type2) {
        if(!(item1 instanceof Dur)) throw numberError(item1, ii);
        if(type1 == YEAR_MONTH_DURATION) return new YMDur((YMDur) item1, (YMDur) item2, true, ii);
        if(type1 == DAY_TIME_DURATION) return new DTDur((DTDur) item1, (DTDur) item2, true, ii);
      }
      if(type1 == DATE_TIME) return new Dtm((Dtm) item1, dur(ii, item2), true, ii);
      if(type2 == DATE_TIME) return new Dtm((Dtm) item2, dur(ii, item1), true, ii);
      if(type1 == DATE) return new Dat((Dat) item1, dur(ii, item2), true, ii);
      if(type2 == DATE) return new Dat((Dat) item2, dur(ii, item1), true, ii);
      if(type1 == TIME && type2 == DAY_TIME_DURATION)
        return new Tim((Tim) item1, (DTDur) item2, true);
      if(type2 == TIME && type1 == DAY_TIME_DURATION)
        return new Tim((Tim) item2, (DTDur) item1, true);
      throw typeError(ii, type1, type2);
    }

    @Override
    public Expr optimize(final Expr expr1, final Expr expr2, final InputInfo info,
        final CompileContext cc) throws QueryException {
      // check for neutral number
      final Type type = numType(expr1.seqType().type, expr2.seqType().type);
      if(expr2 instanceof ANum && ((ANum) expr2).dbl() == 0) {
        return new Cast(cc.sc(), info, expr1, type.seqType()).optimize(cc);
      }
      // check for identical operands
      if(expr1.equals(expr2)) {
        return new Arith(info, expr1, Int.get(2), Calc.MULT).optimize(cc);
      }
      return null;
    }

    @Override
    public Type type(final Type type1, final Type type2) {
      if(type1 == YEAR_MONTH_DURATION && type2 == YEAR_MONTH_DURATION) return YEAR_MONTH_DURATION;
      if(type1 == DAY_TIME_DURATION && type2 == DAY_TIME_DURATION) return DAY_TIME_DURATION;
      if(type1 == DATE_TIME || type2 == DATE_TIME) return DATE_TIME;
      if(type1 == DATE || type2 == DATE) return DATE;
      if(type1 == TIME && type2 == DAY_TIME_DURATION) return TIME;
      if(type1 == DAY_TIME_DURATION && type2 == TIME) return TIME;
      return numType(type1, type2);
    }

    @Override
    public Calc invert() {
      return MINUS;
    }
  },

  /** Subtraction. */
  MINUS("-") {
    @Override
    public Item eval(final Item item1, final Item item2, final InputInfo ii) throws QueryException {
      final Type type1 = item1.type, type2 = item2.type;
      final boolean num1 = type1.isNumberOrUntyped(), num2 = type2.isNumberOrUntyped();
      if(num1 ^ num2) throw numberError(num1 ? item2 : item1, ii);

      if(num1) {
        // numbers or untyped values
        final Type type = numType(type1, type2);
        if(type == INTEGER) {
          final long itr1 = item1.itr(ii), itr2 = item2.itr(ii);
          if(itr2 < 0 ? itr1 > Long.MAX_VALUE + itr2 : itr1 < Long.MIN_VALUE + itr2)
            throw RANGE_X.get(ii, itr1 + " - " + itr2);
          return Int.get(itr1 - itr2);
        }
        if(type == DOUBLE) return Dbl.get(item1.dbl(ii) - item2.dbl(ii));
        if(type == FLOAT) return Flt.get(item1.flt(ii) - item2.flt(ii));
        return Dec.get(item1.dec(ii).subtract(item2.dec(ii)));
      }

      // dates or durations
      if(type1 == type2) {
        if(type1 == DATE_TIME || type1 == DATE || type1 == TIME)
          return new DTDur((ADate) item1, (ADate) item2, ii);
        if(type1 == YEAR_MONTH_DURATION) return new YMDur((YMDur) item1, (YMDur) item2, false, ii);
        if(type1 == DAY_TIME_DURATION) return new DTDur((DTDur) item1, (DTDur) item2, false, ii);
        throw numberError(item1, ii);
      }
      if(type1 == DATE_TIME) return new Dtm((Dtm) item1, dur(ii, item2), false, ii);
      if(type1 == DATE) return new Dat((Dat) item1, dur(ii, item2), false, ii);
      if(type1 == TIME && type2 == DAY_TIME_DURATION)
        return new Tim((Tim) item1, (DTDur) item2, false);
      throw typeError(ii, type1, type2);
    }

    @Override
    public Expr optimize(final Expr expr1, final Expr expr2, final InputInfo info,
        final CompileContext cc) throws QueryException {

      // check for neutral number
      final Type type = numType(expr1.seqType().type, expr2.seqType().type);
      if(expr2 instanceof ANum && ((ANum) expr2).dbl() == 0) {
        return new Cast(cc.sc(), info, expr1, type.seqType()).optimize(cc);
      }
      // check for identical operands; ignore floating numbers due to special cases (NaN, INF)
      if(expr1.equals(expr2)) {
        return type == DECIMAL ? Dec.ZERO : type == INTEGER ? Int.ZERO : null;
      }
      return null;
    }

    @Override
    public Type type(final Type type1, final Type type2) {
      if(type1 == DATE_TIME && type2 == DATE_TIME) return DAY_TIME_DURATION;
      if(type1 == DATE && type2 == DATE) return DAY_TIME_DURATION;
      if(type1 == TIME && type2 == TIME) return DAY_TIME_DURATION;
      if(type1 == DATE_TIME) return DATE_TIME;
      if(type1 == DATE) return DATE;
      if(type1 == TIME && type2 == DAY_TIME_DURATION) return TIME;
      return numType(type1, type2);
    }

    @Override
    public Calc invert() {
      return PLUS;
    }
  },

  /** Multiplication. */
  MULT("*") {
    @Override
    public Item eval(final Item item1, final Item item2, final InputInfo ii) throws QueryException {
      final Type type1 = item1.type, type2 = item2.type;

      if(type1 == YEAR_MONTH_DURATION) {
        if(item2 instanceof ANum) return new YMDur((Dur) item1, item2.dbl(ii), true, ii);
        throw numberError(item2, ii);
      }
      if(type2 == YEAR_MONTH_DURATION) {
        if(item1 instanceof ANum) return new YMDur((Dur) item2, item1.dbl(ii), true, ii);
        throw numberError(item1, ii);
      }
      if(type1 == DAY_TIME_DURATION) {
        if(item2 instanceof ANum) return new DTDur((Dur) item1, item2.dbl(ii), true, ii);
        throw numberError(item2, ii);
      }
      if(type2 == DAY_TIME_DURATION) {
        if(item1 instanceof ANum) return new DTDur((Dur) item2, item1.dbl(ii), true, ii);
        throw numberError(item1, ii);
      }

      final boolean num1 = type1.isNumberOrUntyped(), num2 = type2.isNumberOrUntyped();
      if(num1 ^ num2) throw typeError(ii, type1, type2);
      if(num1) {
        final Type type = numType(type1, type2);
        if(type == INTEGER) {
          final long l1 = item1.itr(ii), l2 = item2.itr(ii);
          if(l2 > 0 ? l1 > Long.MAX_VALUE / l2 || l1 < Long.MIN_VALUE / l2
                    : l2 < -1 ? l1 > Long.MIN_VALUE / l2 || l1 < Long.MAX_VALUE / l2
                              : l2 == -1 && l1 == Long.MIN_VALUE)
            throw RANGE_X.get(ii, l1 + " * " + l2);
          return Int.get(l1 * l2);
        }
        if(type == DOUBLE) return Dbl.get(item1.dbl(ii) * item2.dbl(ii));
        if(type == FLOAT) return Flt.get(item1.flt(ii) * item2.flt(ii));
        return Dec.get(item1.dec(ii).multiply(item2.dec(ii)));
      }
      throw numberError(item1, ii);
    }

    @Override
    public Expr optimize(final Expr expr1, final Expr expr2, final InputInfo info,
        final CompileContext cc) throws QueryException {

      final Type type = numType(expr1.seqType().type, expr2.seqType().type);
      if(expr2 instanceof ANum) {
        final double dbl2 = ((ANum) expr2).dbl();
        // check for neutral number
        if(dbl2 == 1) return new Cast(cc.sc(), info, expr1, type.seqType()).optimize(cc);
        // check for absorbing number
        if(dbl2 == 0) return type == DECIMAL ? Dec.ZERO : type == INTEGER ? Int.ZERO : null;
      }
      // check for identical operands
      if(expr1.equals(expr2) && type == DOUBLE) {
        return cc.function(Function._MATH_POW, info, expr1, Dbl.get(2));
      }
      return null;
    }

    @Override
    public Type type(final Type type1, final Type type2) {
      if(type1 == YEAR_MONTH_DURATION || type2 == YEAR_MONTH_DURATION) return YEAR_MONTH_DURATION;
      if(type1 == DAY_TIME_DURATION || type2 == DAY_TIME_DURATION) return DAY_TIME_DURATION;
      return numType(type1, type2);
    }

    @Override
    public Calc invert() {
      return DIV;
    }
  },

  /** Division. */
  DIV("div") {
    @Override
    public Item eval(final Item item1, final Item item2, final InputInfo ii) throws QueryException {
      final Type type1 = item1.type, type2 = item2.type;
      if(type1 == type2) {
        if(type1 == YEAR_MONTH_DURATION) {
          final BigDecimal bd = BigDecimal.valueOf(((YMDur) item2).ymd());
          if(bd.doubleValue() == 0.0) throw zeroError(ii, item1);
          return Dec.get(BigDecimal.valueOf(((YMDur) item1).ymd()).
              divide(bd, MathContext.DECIMAL64));
        }
        if(type1 == DAY_TIME_DURATION) {
          final BigDecimal bd = ((DTDur) item2).dtd();
          if(bd.doubleValue() == 0.0) throw zeroError(ii, item1);
          return Dec.get(((DTDur) item1).dtd().divide(bd, MathContext.DECIMAL64));
        }
      }
      if(type1 == YEAR_MONTH_DURATION) {
        if(item2 instanceof ANum) return new YMDur((Dur) item1, item2.dbl(ii), false, ii);
        throw numberError(item2, ii);
      }
      if(type1 == DAY_TIME_DURATION) {
        if(item2 instanceof ANum) return new DTDur((Dur) item1, item2.dbl(ii), false, ii);
        throw numberError(item2, ii);
      }

      checkNum(ii, item1, item2);
      final Type type = numType(type1, type2);
      if(type == DOUBLE) return Dbl.get(item1.dbl(ii) / item2.dbl(ii));
      if(type == FLOAT) return Flt.get(item1.flt(ii) / item2.flt(ii));

      final BigDecimal dec1 = item1.dec(ii), dec2 = item2.dec(ii);
      if(dec2.signum() == 0) throw zeroError(ii, item1);
      final int scale = Math.max(18, Math.max(dec1.scale(), dec2.scale()));
      return Dec.get(dec1.divide(dec2, scale, RoundingMode.HALF_EVEN));
    }

    @Override
    public Expr optimize(final Expr expr1, final Expr expr2, final InputInfo info,
        final CompileContext cc) throws QueryException {

      // check for neutral number
      final Type type = numType(expr1.seqType().type, expr2.seqType().type);
      if(expr2 instanceof ANum && ((ANum) expr2).dbl() == 1) {
        return new Cast(cc.sc(), info, expr1, type.seqType()).optimize(cc);
      }
      // check for identical operands; ignore floating numbers due to special cases (NaN, INF)
      if(expr1.equals(expr2)) {
        return type == DECIMAL ? Dec.ONE : type == INTEGER ? Int.ONE : null;
      }
      return null;
    }

    @Override
    public Type type(final Type type1, final Type type2) {
      if(type1 == YEAR_MONTH_DURATION && type2 == YEAR_MONTH_DURATION ||
         type1 == DAY_TIME_DURATION && type2 == DAY_TIME_DURATION) return DECIMAL;
      if(type1 == YEAR_MONTH_DURATION) return YEAR_MONTH_DURATION;
      if(type1 == DAY_TIME_DURATION) return DAY_TIME_DURATION;
      final Type type = numType(type1, type2);
      return type == INTEGER ? DECIMAL : type;
    }

    @Override
    public Calc invert() {
      return MULT;
    }
  },

  /** Integer division. */
  IDIV("idiv") {
    @Override
    public Int eval(final Item item1, final Item item2, final InputInfo ii)
        throws QueryException {
      checkNum(ii, item1, item2);
      final Type type = numType(item1.type, item2.type);
      if(type == DOUBLE || type == FLOAT) {
        final double dbl1 = item1.dbl(ii), dbl2 = item2.dbl(ii);
        if(dbl2 == 0) throw zeroError(ii, item1);
        final double dbl = dbl1 / dbl2;
        if(Double.isNaN(dbl) || Double.isInfinite(dbl))
          throw DIVFLOW_X.get(ii, dbl1 + " idiv " + dbl2);
        if(dbl < Long.MIN_VALUE || dbl > Long.MAX_VALUE)
          throw RANGE_X.get(ii, dbl1 + " idiv " + dbl2);
        return Int.get((long) dbl);
      }

      if(type == INTEGER) {
        final long itr1 = item1.itr(ii), itr2 = item2.itr(ii);
        if(itr2 == 0) throw zeroError(ii, item1);
        if(itr1 == Integer.MIN_VALUE && itr2 == -1) throw RANGE_X.get(ii, itr1 + " idiv " + itr2);
        return Int.get(itr1 / itr2);
      }

      final BigDecimal dec1 = item1.dec(ii), dec2 = item2.dec(ii);
      if(dec2.signum() == 0) throw zeroError(ii, item1);
      final BigDecimal dec = dec1.divideToIntegralValue(dec2);
      if(!(MIN_LONG.compareTo(dec) <= 0 && dec.compareTo(MAX_LONG) <= 0))
        throw RANGE_X.get(ii, dec1 + " idiv " + dec2);
      return Int.get(dec.longValueExact());
    }

    @Override
    public Expr optimize(final Expr expr1, final Expr expr2, final InputInfo info,
        final CompileContext cc) throws QueryException {

      // check for neutral number
      final Type type = numType(expr1.seqType().type, expr2.seqType().type);
      if(expr2 instanceof ANum && ((ANum) expr2).dbl() == 1) {
        return new Cast(cc.sc(), info, expr1, SeqType.INTEGER_O).optimize(cc);
      }
      // check for identical operands; ignore floating numbers due to special cases (NaN, INF)
      if(expr1.equals(expr2)) {
        return type == DECIMAL ? Dec.ONE : type == INTEGER ? Int.ONE : null;
      }
      return null;
    }

    @Override
    public Type type(final Type type1, final Type type2) {
      return INTEGER;
    }

    @Override
    public Calc invert() {
      return null;
    }
  },

  /** Modulo. */
  MOD("mod") {
    @Override
    public Item eval(final Item item1, final Item item2, final InputInfo ii) throws QueryException {
      checkNum(ii, item1, item2);
      final Type type = numType(item1.type, item2.type);
      if(type == DOUBLE) return Dbl.get(item1.dbl(ii) % item2.dbl(ii));
      if(type == FLOAT) return Flt.get(item1.flt(ii) % item2.flt(ii));
      if(type == INTEGER) {
        final long itr1 = item1.itr(ii), itr2 = item2.itr(ii);
        if(itr2 == 0) throw zeroError(ii, item1);
        return Int.get(itr1 % itr2);
      }

      final BigDecimal dec1 = item1.dec(ii), dec2 = item2.dec(ii);
      if(dec2.signum() == 0) throw zeroError(ii, item1);
      final BigDecimal sub = dec1.divide(dec2, 0, RoundingMode.DOWN);
      return Dec.get(dec1.subtract(sub.multiply(dec2)));
    }

    @Override
    public Expr optimize(final Expr expr1, final Expr expr2, final InputInfo info,
        final CompileContext cc) {

      // check for neutral number
      final Type type = numType(expr1.seqType().type, expr2.seqType().type);
      if(type == INTEGER && expr2 == Int.ONE) return Int.ZERO;
      return null;
    }

    @Override
    public Type type(final Type type1, final Type type2) {
      final Type type = numType(type1, type2);
      return type == ANY_ATOMIC_TYPE ? NUMERIC : type;
    }

    @Override
    public Calc invert() {
      return null;
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
   * @param item1 first item
   * @param item2 second item
   * @param ii input info
   * @return result type
   * @throws QueryException query exception
   */
  public abstract Item eval(Item item1, Item item2, InputInfo ii) throws QueryException;

  /**
   * Optimizes the expressions.
   * @param expr1 first expression
   * @param expr2 second expression
   * @param info input info
   * @param cc compilation context
   * @return result expression, or {@code null} if expression cannot be optimized
   * @throws QueryException query exception
   */
  public abstract Expr optimize(Expr expr1, Expr expr2, InputInfo info, CompileContext cc)
      throws QueryException;

  /**
   * Returns the result type of this calculation.
   * @param type1 first item type
   * @param type2 second item type
   * @return result expression, or {@code null} if expression cannot be optimized
   */
  public abstract Type type(Type type1, Type type2);

  /**
   * Inverts the operator.
   * @return inverted operator or {@code null}
   */
  public abstract Calc invert();

  /**
   * Returns the numeric type with the highest precedence.
   * @param type1 first item type
   * @param type2 second item type
   * @return type
   */
  public static Type numType(final Type type1, final Type type2) {
    if(!type1.isNumberOrUntyped() || !type2.isNumberOrUntyped()) return ANY_ATOMIC_TYPE;
    if(type1 == DOUBLE || type2 == DOUBLE || type1.isUntyped() || type2.isUntyped()) return DOUBLE;
    if(type1 == FLOAT || type2 == FLOAT) return FLOAT;
    if(type1 == DECIMAL || type2 == DECIMAL) return DECIMAL;
    if(type1 == NUMERIC || type2 == NUMERIC) return NUMERIC;
    return INTEGER;
  }

  /**
   * Throws a division by zero exception.
   * @param ii input info
   * @param item item
   * @return query exception (indicates that an error is raised)
   */
  private static QueryException zeroError(final InputInfo ii, final Item item) {
    return DIVZERO_X.get(ii, normalize(item, ii));
  }

  /**
   * Returns a type error.
   * @param ii input info
   * @param type1 first type
   * @param type2 second type
   * @return query exception
   */
  final QueryException typeError(final InputInfo ii, final Type type1, final Type type2) {
    return CALCTYPE_X_X_X.get(ii, info(), type1, type2);
  }

  /**
   * Returns a duration type error.
   * @param ii input info
   * @param item item
   * @return duration
   * @throws QueryException query exception
   */
  static Dur dur(final InputInfo ii, final Item item) throws QueryException {
    if(item instanceof Dur) {
      if(item.type == DURATION) throw NOSUBDUR_X.get(ii, item);
      return (Dur) item;
    }
    throw NODUR_X_X.get(ii, item.type, item);
  }

  /**
   * Checks if the specified items are numeric or untyped.
   * @param ii input info
   * @param item1 first item
   * @param item2 second item
   * @throws QueryException query exception
   */
  static void checkNum(final InputInfo ii, final Item item1, final Item item2)
      throws QueryException {
    if(!item1.type.isNumberOrUntyped()) throw numberError(item1, ii);
    if(!item2.type.isNumberOrUntyped()) throw numberError(item2, ii);
  }

  /**
   * Returns a string representation of the operator.
   * @return string
   */
  final String info() {
    return '\'' + name + "' expression";
  }

  @Override
  public String toString() {
    return name;
  }
}
