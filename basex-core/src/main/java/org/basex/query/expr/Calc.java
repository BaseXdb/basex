package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.AtomType.*;

import java.math.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Calculation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public enum Calc {
  /** Addition. */
  PLUS("+") {
    @Override
    public Item eval(final Item item1, final Item item2, final InputInfo info)
        throws QueryException {
      final Type t1 = item1.type, t2 = item2.type;
      final boolean n1 = t1.isNumberOrUntyped(), n2 = t2.isNumberOrUntyped();
      if(n1 ^ n2) throw numberError(n1 ? item2 : item1, info);

      if(n1) {
        // numbers or untyped values
        final Type t = type(t1, t2);
        if(t == ITR) {
          final long l1 = item1.itr(info), l2 = item2.itr(info);
          if(l2 > 0 ? l1 > Long.MAX_VALUE - l2 : l1 < Long.MIN_VALUE - l2)
            throw RANGE_X.get(info, l1 + " + " + l2);
          return Int.get(l1 + l2);
        }
        if(t == DBL) return Dbl.get(item1.dbl(info) + item2.dbl(info));
        if(t == FLT) return Flt.get(item1.flt(info) + item2.flt(info));
        return Dec.get(item1.dec(info).add(item2.dec(info)));
      }

      // dates or durations
      if(t1 == t2) {
        if(!(item1 instanceof Dur)) throw numberError(item1, info);
        if(t1 == YMD) return new YMDur((YMDur) item1, (YMDur) item2, true, info);
        if(t1 == DTD) return new DTDur((DTDur) item1, (DTDur) item2, true, info);
      }
      if(t1 == DTM) return new Dtm((Dtm) item1, dur(info, item2), true, info);
      if(t2 == DTM) return new Dtm((Dtm) item2, dur(info, item1), true, info);
      if(t1 == DAT) return new Dat((Dat) item1, dur(info, item2), true, info);
      if(t2 == DAT) return new Dat((Dat) item2, dur(info, item1), true, info);
      if(t1 == TIM && t2 == DTD) return new Tim((Tim) item1, (DTDur) item2, true);
      if(t2 == TIM && t1 == DTD) return new Tim((Tim) item2, (DTDur) item1, true);
      throw typeError(info, t1, t2);
    }

    @Override
    public Expr optimize(final Expr ex1, final Expr ex2) throws QueryException {
      // check for neutral numbers
      final BiFunction<Expr, Expr, Expr> func = (e1, e2) ->
        e1 instanceof ANum && ((ANum) e1).dbl() == 0 ? e2 : null;
      final Expr expr = func.apply(ex1, ex2);
      return expr != null ? expr : func.apply(ex2, ex1);
    }
  },

  /** Subtraction. */
  MINUS("-") {
    @Override
    public Item eval(final Item item1, final Item item2, final InputInfo info)
        throws QueryException {
      final Type t1 = item1.type, t2 = item2.type;
      final boolean n1 = t1.isNumberOrUntyped(), n2 = t2.isNumberOrUntyped();
      if(n1 ^ n2) throw numberError(n1 ? item2 : item1, info);

      if(n1) {
        // numbers or untyped values
        final Type t = type(t1, t2);
        if(t == ITR) {
          final long l1 = item1.itr(info), l2 = item2.itr(info);
          if(l2 < 0 ? l1 > Long.MAX_VALUE + l2 : l1 < Long.MIN_VALUE + l2)
            throw RANGE_X.get(info, l1 + " - " + l2);
          return Int.get(l1 - l2);
        }
        if(t == DBL) return Dbl.get(item1.dbl(info) - item2.dbl(info));
        if(t == FLT) return Flt.get(item1.flt(info) - item2.flt(info));
        return Dec.get(item1.dec(info).subtract(item2.dec(info)));
      }

      // dates or durations
      if(t1 == t2) {
        if(t1 == DTM || t1 == DAT || t1 == TIM)
          return new DTDur((ADate) item1, (ADate) item2, info);
        if(t1 == YMD) return new YMDur((YMDur) item1, (YMDur) item2, false, info);
        if(t1 == DTD) return new DTDur((DTDur) item1, (DTDur) item2, false, info);
        throw numberError(item1, info);
      }
      if(t1 == DTM) return new Dtm((Dtm) item1, dur(info, item2), false, info);
      if(t1 == DAT) return new Dat((Dat) item1, dur(info, item2), false, info);
      if(t1 == TIM && t2 == DTD) return new Tim((Tim) item1, (DTDur) item2, false);
      throw typeError(info, t1, t2);
    }

    @Override
    public Expr optimize(final Expr ex1, final Expr ex2) throws QueryException {
      // check for neutral number and identical arguments
      return ex2 instanceof ANum && ((ANum) ex2).dbl() == 0 ? ex1 :
        ex1.equals(ex2) ? zero(ex1) : null;
    }
  },

  /** Multiplication. */
  MULT("*") {
    @Override
    public Item eval(final Item item1, final Item item2, final InputInfo info)
        throws QueryException {
      final Type t1 = item1.type, t2 = item2.type;
      if(t1 == YMD) {
        if(item2 instanceof ANum) return new YMDur((Dur) item1, item2.dbl(info), true, info);
        throw numberError(item2, info);
      }
      if(t2 == YMD) {
        if(item1 instanceof ANum) return new YMDur((Dur) item2, item1.dbl(info), true, info);
        throw numberError(item1, info);
      }
      if(t1 == DTD) {
        if(item2 instanceof ANum) return new DTDur((Dur) item1, item2.dbl(info), true, info);
        throw numberError(item2, info);
      }
      if(t2 == DTD) {
        if(item1 instanceof ANum) return new DTDur((Dur) item2, item1.dbl(info), true, info);
        throw numberError(item1, info);
      }

      final boolean b1 = t1.isNumberOrUntyped(), b2 = t2.isNumberOrUntyped();
      if(b1 ^ b2) throw typeError(info, t1, t2);
      if(b1) {
        final Type t = type(t1, t2);
        if(t == ITR) {
          final long l1 = item1.itr(info);
          final long l2 = item2.itr(info);
          if(l2 > 0 ? l1 > Long.MAX_VALUE / l2 || l1 < Long.MIN_VALUE / l2
                    : l2 < -1 ? l1 > Long.MIN_VALUE / l2 || l1 < Long.MAX_VALUE / l2
                              : l2 == -1 && l1 == Long.MIN_VALUE)
            throw RANGE_X.get(info, l1 + " * " + l2);
          return Int.get(l1 * l2);
        }
        if(t == DBL) return Dbl.get(item1.dbl(info) * item2.dbl(info));
        if(t == FLT) return Flt.get(item1.flt(info) * item2.flt(info));
        return Dec.get(item1.dec(info).multiply(item2.dec(info)));
      }
      throw numberError(item1, info);
    }

    @Override
    public Expr optimize(final Expr ex1, final Expr ex2) throws QueryException {
      // check for absorbing and neutral numbers
      final BiFunction<Expr, Expr, Expr> func = (e1, e2) -> {
        final double d1 = e1 instanceof ANum ? ((ANum) e1).dbl() : Double.NaN;
        return d1 == 1 ? e2 : d1 == 0 ? zero(e1) : null;
      };
      final Expr expr = func.apply(ex1, ex2);
      return expr != null ? expr : func.apply(ex2, ex1);
    }
  },

  /** Division. */
  DIV("div") {
    @Override
    public Item eval(final Item item1, final Item item2, final InputInfo info)
        throws QueryException {
      final Type t1 = item1.type, t2 = item2.type;
      if(t1 == t2) {
        if(t1 == YMD) {
          final BigDecimal bd = BigDecimal.valueOf(((YMDur) item2).ymd());
          if(bd.doubleValue() == 0.0) throw zeroError(info, item1);
          return Dec.get(BigDecimal.valueOf(((YMDur) item1).ymd()).
              divide(bd, MathContext.DECIMAL64));
        }
        if(t1 == DTD) {
          final BigDecimal bd = ((DTDur) item2).dtd();
          if(bd.doubleValue() == 0.0) throw zeroError(info, item1);
          return Dec.get(((DTDur) item1).dtd().divide(bd, MathContext.DECIMAL64));
        }
      }
      if(t1 == YMD) {
        if(item2 instanceof ANum) return new YMDur((Dur) item1, item2.dbl(info), false, info);
        throw numberError(item2, info);
      }
      if(t1 == DTD) {
        if(item2 instanceof ANum) return new DTDur((Dur) item1, item2.dbl(info), false, info);
        throw numberError(item2, info);
      }

      checkNum(info, item1, item2);
      final Type t = type(t1, t2);
      if(t == DBL) return Dbl.get(item1.dbl(info) / item2.dbl(info));
      if(t == FLT) return Flt.get(item1.flt(info) / item2.flt(info));

      final BigDecimal b1 = item1.dec(info), b2 = item2.dec(info);
      if(b2.signum() == 0) throw zeroError(info, item1);
      final int s = Math.max(18, Math.max(b1.scale(), b2.scale()));
      return Dec.get(b1.divide(b2, s, RoundingMode.HALF_EVEN));
    }

    @Override
    public Expr optimize(final Expr ex1, final Expr ex2) throws QueryException {
      // check for neutral number and identical arguments
      return ex2 instanceof ANum && ((ANum) ex2).dbl() == 1 ? ex1 :
        ex1.equals(ex2) ? one(ex1) : null;
    }
  },

  /** Integer division. */
  IDIV("idiv") {
    @Override
    public Int eval(final Item item1, final Item ti2, final InputInfo info) throws QueryException {
      checkNum(info, item1, ti2);
      final Type t = type(item1.type, ti2.type);
      if(t == DBL || t == FLT) {
        final double d1 = item1.dbl(info), d2 = ti2.dbl(info);
        if(d2 == 0) throw zeroError(info, item1);
        final double d = d1 / d2;
        if(Double.isNaN(d) || Double.isInfinite(d)) throw DIVFLOW_X.get(info, d1 + " idiv " + d2);
        if(d < Long.MIN_VALUE || d > Long.MAX_VALUE) throw RANGE_X.get(info, d1 + " idiv " + d2);
        return Int.get((long) d);
      }

      if(t == ITR) {
        final long b1 = item1.itr(info), b2 = ti2.itr(info);
        if(b2 == 0) throw zeroError(info, item1);
        if(b1 == Integer.MIN_VALUE && b2 == -1) throw RANGE_X.get(info, b1 + " idiv " + b2);
        return Int.get(b1 / b2);
      }

      final BigDecimal b1 = item1.dec(info), b2 = ti2.dec(info);
      if(b2.signum() == 0) throw zeroError(info, item1);
      final BigDecimal res = b1.divideToIntegralValue(b2);
      if(!(MIN_LONG.compareTo(res) <= 0 && res.compareTo(MAX_LONG) <= 0))
        throw RANGE_X.get(info, b1 + " idiv " + b2);
      return Int.get(res.longValueExact());
    }

    @Override
    public Expr optimize(final Expr ex1, final Expr ex2) throws QueryException {
      // check for neutral number and identical arguments
      return ex2 instanceof ANum && ((ANum) ex2).dbl() == 1 ? ex1 :
        ex1.equals(ex2) ? one(ex1) : null;
    }
  },

  /** Modulo. */
  MOD("mod") {
    @Override
    public Item eval(final Item item1, final Item item2, final InputInfo info)
        throws QueryException {
      checkNum(info, item1, item2);
      final Type t = type(item1.type, item2.type);
      if(t == DBL) return Dbl.get(item1.dbl(info) % item2.dbl(info));
      if(t == FLT) return Flt.get(item1.flt(info) % item2.flt(info));
      if(t == ITR) {
        final long b1 = item1.itr(info), b2 = item2.itr(info);
        if(b2 == 0) throw zeroError(info, item1);
        return Int.get(b1 % b2);
      }

      final BigDecimal b1 = item1.dec(info), b2 = item2.dec(info);
      if(b2.signum() == 0) throw zeroError(info, item1);
      final BigDecimal q = b1.divide(b2, 0, RoundingMode.DOWN);
      return Dec.get(b1.subtract(q.multiply(b2)));
    }

    @Override
    public Expr optimize(final Expr ex1, final Expr ex2) throws QueryException {
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
   * @param info input info
   * @return result type
   * @throws QueryException query exception
   */
  public abstract Item eval(Item item1, Item item2, InputInfo info) throws QueryException;

  /**
   * Optimizes the expressions.
   * @param ex1 first expression
   * @param ex2 second expression
   * @return result type
   * @throws QueryException query exception
   */
  public abstract Expr optimize(Expr ex1, Expr ex2) throws QueryException;

  /**
   * Returns the numeric type with the highest precedence.
   * @param type1 first item type
   * @param type2 second item type
   * @return type
   */
  public static Type type(final Type type1, final Type type2) {
    if(type1 == DBL || type2 == DBL || type1.isUntyped() || type2.isUntyped()) return DBL;
    if(type1 == FLT || type2 == FLT) return FLT;
    if(type1 == DEC || type2 == DEC) return DEC;
    return ITR;
  }

  /**
   * Tries to rewrite the expression to {@code 0}.
   * @param expr expression
   * @return zero value or {@code null}
   */
  private static Expr zero(final Expr expr) {
    // floating points
    final Type t = expr.seqType().type;
    return t == DEC ? Dec.ZERO : t.instanceOf(AtomType.ITR) ? Int.ZERO : null;
  }

  /**
   * Tries to rewrite the expression to {@code 1}.
   * @param expr expression
   * @return zero value or {@code null}
   */
  private static Expr one(final Expr expr) {
    // floating points
    final Type t = expr.seqType().type;
    return t == DEC ? Dec.ONE : t.instanceOf(AtomType.ITR) ? Int.ONE : null;
  }

  /**
   * Throws a division by zero exception.
   * @param info input info
   * @param item item
   * @return query exception (indicates that an error is raised)
   */
  private static QueryException zeroError(final InputInfo info, final Item item) {
    return DIVZERO_X.get(info, chop(item, info));
  }

  /**
   * Returns a type error.
   * @param info input info
   * @param type1 first type
   * @param type2 second type
   * @return query exception
   */
  final QueryException typeError(final InputInfo info, final Type type1, final Type type2) {
    return CALCTYPE_X_X_X.get(info, info(), type1, type2);
  }

  /**
   * Returns a duration type error.
   * @param info input info
   * @param item item
   * @return duration
   * @throws QueryException query exception
   */
  static Dur dur(final InputInfo info, final Item item) throws QueryException {
    if(item instanceof Dur) {
      if(item.type == DUR) throw NOSUBDUR_X.get(info, item);
      return (Dur) item;
    }
    throw NODUR_X_X.get(info, item.type, item);
  }

  /**
   * Checks if the specified items are numeric or untyped.
   * @param info input info
   * @param item1 first item
   * @param item2 second item
   * @throws QueryException query exception
   */
  static void checkNum(final InputInfo info, final Item item1, final Item item2)
      throws QueryException {
    if(!item1.type.isNumberOrUntyped()) throw numberError(item1, info);
    if(!item2.type.isNumberOrUntyped()) throw numberError(item2, info);
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
