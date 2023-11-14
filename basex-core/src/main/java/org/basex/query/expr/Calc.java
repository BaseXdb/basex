package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.basex.query.value.type.AtomType.*;
import static org.basex.query.expr.CalcOpt.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Calculation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public enum Calc {
  /** Addition. */
  ADD("+") {
    @Override
    public Item eval(final Item item1, final Item item2, final InputInfo info)
        throws QueryException {
      final Type type1 = item1.type, type2 = item2.type;
      final boolean num1 = type1.isNumberOrUntyped(), num2 = type2.isNumberOrUntyped();
      if(num1 ^ num2) throw numberError(num1 ? item2 : item1, info);

      // numbers or untyped values
      if(num1) {
        switch(numType(type1, type2)) {
          case INTEGER: return addInt(item1, item2, info);
          case DOUBLE:  return addDbl(item1, item2, info);
          case FLOAT: return addFlt(item1, item2, info);
          default: return addDec(item1, item2, info);
        }
      }

      // dates or durations
      if(type1 == type2) {
        if(!(item1 instanceof Dur)) throw numberError(item1, info);
        if(type1 == YEAR_MONTH_DURATION) return new YMDur((YMDur) item1, (YMDur) item2, true, info);
        if(type1 == DAY_TIME_DURATION) return new DTDur((DTDur) item1, (DTDur) item2, true, info);
      }
      if(type1 == DATE_TIME) return new Dtm((Dtm) item1, dur(info, item2), true, info);
      if(type2 == DATE_TIME) return new Dtm((Dtm) item2, dur(info, item1), true, info);
      if(type1 == DATE) return new Dat((Dat) item1, dur(info, item2), true, info);
      if(type2 == DATE) return new Dat((Dat) item2, dur(info, item1), true, info);
      if(type1 == TIME && type2 == DAY_TIME_DURATION)
        return new Tim((Tim) item1, (DTDur) item2, true);
      if(type2 == TIME && type1 == DAY_TIME_DURATION)
        return new Tim((Tim) item2, (DTDur) item1, true);
      throw typeError(info, type1, type2);
    }

    @Override
    public Expr optimize(final Expr expr1, final Expr expr2, final InputInfo info,
        final CompileContext cc) throws QueryException {
      // check for neutral number
      final Type type = numType(expr1.seqType().type, expr2.seqType().type);
      if(expr2 instanceof ANum && ((ANum) expr2).dbl() == 0) {
        return new Cast(cc.sc(), info, expr1, type.seqType()).optimize(cc);
      }
      // merge arithmetical expressions
      if(expr1.equals(expr2)) {
        return new Arith(info, expr1, Int.get(2), MULTIPLY).optimize(cc);
      }
      if(expr2 instanceof Unary) {
        return new Arith(info, expr1, ((Unary) expr2).expr, SUBTRACT).optimize(cc);
      }
      if(expr1 instanceof Arith) {
        final Arith arith = (Arith) expr1;
        if(arith.calc == MULTIPLY && arith.exprs[0].equals(expr2) &&
            arith.exprs[1] instanceof Int) {
          final long factor = ((Int) arith.exprs[1]).itr();
          return new Arith(info, arith.exprs[0], Int.get(factor + 1), MULTIPLY).optimize(cc);
        }
      }
      return null;
    }

    @Override
    public Type type(final Type type1, final Type type2) {
      if(type1 == YEAR_MONTH_DURATION && type2 == YEAR_MONTH_DURATION) return YEAR_MONTH_DURATION;
      if(type1 == DAY_TIME_DURATION && type2 == DAY_TIME_DURATION) return DAY_TIME_DURATION;
      if(type1 == DATE_TIME || type2 == DATE_TIME) return DATE_TIME;
      if(type1 == DATE || type2 == DATE) return DATE;
      if(type1 == TIME && type2 == DAY_TIME_DURATION ||
         type1 == DAY_TIME_DURATION && type2 == TIME) return TIME;
      return numType(type1, type2);
    }

    @Override
    public Calc invert() {
      return SUBTRACT;
    }
  },

  /** Subtraction. */
  SUBTRACT("-") {
    @Override
    public Item eval(final Item item1, final Item item2, final InputInfo info)
        throws QueryException {
      final Type type1 = item1.type, type2 = item2.type;
      final boolean num1 = type1.isNumberOrUntyped(), num2 = type2.isNumberOrUntyped();
      if(num1 ^ num2) throw numberError(num1 ? item2 : item1, info);

      // numbers or untyped values
      if(num1) {
        switch(numType(type1, type2)) {
          case INTEGER: return subtractInt(item1, item2, info);
          case DOUBLE:  return subtractDbl(item1, item2, info);
          case FLOAT: return subtractFlt(item1, item2, info);
          default: return subtractDec(item1, item2, info);
        }
      }

      // dates or durations
      if(type1 == type2) {
        if(type1 == DATE_TIME || type1 == DATE || type1 == TIME)
          return new DTDur((ADate) item1, (ADate) item2, info);
        if(type1 == YEAR_MONTH_DURATION)
          return new YMDur((YMDur) item1, (YMDur) item2, false, info);
        if(type1 == DAY_TIME_DURATION)
          return new DTDur((DTDur) item1, (DTDur) item2, false, info);
        throw numberError(item1, info);
      }
      if(type1 == DATE_TIME) return new Dtm((Dtm) item1, dur(info, item2), false, info);
      if(type1 == DATE) return new Dat((Dat) item1, dur(info, item2), false, info);
      if(type1 == TIME && type2 == DAY_TIME_DURATION)
        return new Tim((Tim) item1, (DTDur) item2, false);
      throw typeError(info, type1, type2);
    }

    @Override
    public Expr optimize(final Expr expr1, final Expr expr2, final InputInfo info,
        final CompileContext cc) throws QueryException {

      // check for neutral number
      final Type type = numType(expr1.seqType().type, expr2.seqType().type);
      if(expr2 instanceof ANum && ((ANum) expr2).dbl() == 0) {
        return new Cast(cc.sc(), info, expr1, type.seqType()).optimize(cc);
      }
      // replace with neutral number; ignore floating numbers due to special cases (NaN, INF)
      if(expr1.equals(expr2)) {
        return type == DECIMAL ? Dec.ZERO : type == INTEGER ? Int.ZERO : null;
      }
      // merge arithmetical expressions; ignore floating numbers due to special cases (NaN, INF)
      if(expr2 instanceof Unary) {
        return new Arith(info, expr1, ((Unary) expr2).expr, ADD).optimize(cc);
      }
      if(expr1 instanceof Arith) {
        final Arith arith = (Arith) expr1;
        if(arith.calc == MULTIPLY && arith.exprs[0].equals(expr2) &&
            arith.exprs[1] instanceof Int) {
          final long factor = ((Int) arith.exprs[1]).itr();
          return new Arith(info, arith.exprs[0], Int.get(factor - 1), MULTIPLY).optimize(cc);
        }
      }
      return null;
    }

    @Override
    public Type type(final Type type1, final Type type2) {
      if(type1 == DATE_TIME && type2 == DATE_TIME ||
         type1 == DATE && type2 == DATE) return DAY_TIME_DURATION;
      if(type1 == TIME && type2 == TIME) return DAY_TIME_DURATION;
      if(type1 == DATE_TIME) return DATE_TIME;
      if(type1 == DATE) return DATE;
      if(type1 == TIME && type2 == DAY_TIME_DURATION) return TIME;
      return numType(type1, type2);
    }

    @Override
    public Calc invert() {
      return ADD;
    }
  },

  /** Multiplication. */
  MULTIPLY("*") {
    @Override
    public Item eval(final Item item1, final Item item2, final InputInfo info)
        throws QueryException {
      final Type type1 = item1.type, type2 = item2.type;
      if(type1 == YEAR_MONTH_DURATION) {
        if(item2 instanceof ANum) return new YMDur((Dur) item1, item2.dbl(info), true, info);
        throw numberError(item2, info);
      }
      if(type2 == YEAR_MONTH_DURATION) {
        if(item1 instanceof ANum) return new YMDur((Dur) item2, item1.dbl(info), true, info);
        throw numberError(item1, info);
      }
      if(type1 == DAY_TIME_DURATION) {
        if(item2 instanceof ANum) return new DTDur((Dur) item1, item2.dbl(info), true, info);
        throw numberError(item2, info);
      }
      if(type2 == DAY_TIME_DURATION) {
        if(item1 instanceof ANum) return new DTDur((Dur) item2, item1.dbl(info), true, info);
        throw numberError(item1, info);
      }

      final boolean num1 = type1.isNumberOrUntyped(), num2 = type2.isNumberOrUntyped();
      if(num1 ^ num2) throw typeError(info, type1, type2);
      // numbers or untyped values
      if(num1) {
        switch(numType(type1, type2)) {
          case INTEGER: return multiplyInt(item1, item2, info);
          case DOUBLE:  return multiplyDbl(item1, item2, info);
          case FLOAT: return multiplyFlt(item1, item2, info);
          default: return multiplyDec(item1, item2, info);
        }
      }
      throw numberError(item1, info);
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
      // merge arithmetical expressions
      if(type == DOUBLE) {
        if(expr1.equals(expr2)) {
          return cc.function(_MATH_POW, info, expr1, Dbl.get(2));
        }
        if(_MATH_POW.is(expr1)) {
          final StandardFunc func = (StandardFunc) expr1;
          if(func.exprs[0].equals(expr2) && func.exprs[1] instanceof ANum) {
            final double factor = ((ANum) func.exprs[1]).dbl();
            return cc.function(_MATH_POW, info, func.exprs[0], Dbl.get(factor + 1));
          }
        }
      }
      if(expr2 instanceof Arith) {
        final Arith arith = (Arith) expr2;
        if(arith.calc == DIVIDE && arith.exprs[0] instanceof Int && arith.exprs[1].equals(expr1)) {
          return new Cast(cc.sc(), info, arith.exprs[0], type.seqType()).optimize(cc);
        }
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
      return DIVIDE;
    }
  },

  /** Division. */
  DIVIDE("div") {
    @Override
    public Item eval(final Item item1, final Item item2, final InputInfo info)
        throws QueryException {
      final Type type1 = item1.type, type2 = item2.type;
      if(type1 == type2) {
        if(type1 == YEAR_MONTH_DURATION) {
          final BigDecimal bd = BigDecimal.valueOf(((YMDur) item2).ymd());
          if(bd.doubleValue() == 0.0) throw DIVZERO_X.get(info, item1);
          return Dec.get(BigDecimal.valueOf(((YMDur) item1).ymd()).
              divide(bd, MathContext.DECIMAL64));
        }
        if(type1 == DAY_TIME_DURATION) {
          final BigDecimal bd = ((DTDur) item2).dtd();
          if(bd.doubleValue() == 0.0) throw DIVZERO_X.get(info, item1);
          return Dec.get(((DTDur) item1).dtd().divide(bd, MathContext.DECIMAL64));
        }
      }
      if(type1 == YEAR_MONTH_DURATION) {
        if(item2 instanceof ANum) return new YMDur((Dur) item1, item2.dbl(info), false, info);
        throw numberError(item2, info);
      }
      if(type1 == DAY_TIME_DURATION) {
        if(item2 instanceof ANum) return new DTDur((Dur) item1, item2.dbl(info), false, info);
        throw numberError(item2, info);
      }

      // numbers or untyped values
      if(!type1.isNumberOrUntyped()) throw numberError(item1, info);
      if(!type2.isNumberOrUntyped()) throw numberError(item2, info);
      switch(numType(type1, type2)) {
        case DOUBLE:  return divideDbl(item1, item2, info);
        case FLOAT: return divideFlt(item1, item2, info);
        default: return divideDec(item1, item2, info);
      }
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
      if(_MATH_POW.is(expr1)) {
        final StandardFunc func = (StandardFunc) expr1;
        if(func.exprs[0].equals(expr2) && func.exprs[1] instanceof ANum) {
          final double factor = ((ANum) func.exprs[1]).dbl();
          return cc.function(_MATH_POW, info, func.exprs[0], Dbl.get(factor - 1));
        }
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
      return MULTIPLY;
    }
  },

  /** Integer division. */
  DIVIDEINT("idiv") {
    @Override
    public Int eval(final Item item1, final Item item2, final InputInfo info)
        throws QueryException {

      // numbers or untyped values
      final Type type1 = item1.type, type2 = item2.type;
      if(!type1.isNumberOrUntyped()) throw numberError(item1, info);
      if(!type2.isNumberOrUntyped()) throw numberError(item2, info);
      switch(numType(type1, type2)) {
        case INTEGER: return divideIntInt(item1, item2, info);
        case DOUBLE:  return divideIntDbl(item1, item2, info);
        case FLOAT: return divideIntFlt(item1, item2, info);
        default: return divideIntDec(item1, item2, info);
      }
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
        return type.oneOf(DECIMAL, INTEGER) ? Int.ONE : null;
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
  MODULO("mod") {
    @Override
    public Item eval(final Item item1, final Item item2, final InputInfo info)
        throws QueryException {
      final Type type1 = item1.type, type2 = item2.type;
      if(!type1.isNumberOrUntyped()) throw numberError(item1, info);
      if(!type2.isNumberOrUntyped()) throw numberError(item2, info);
      switch(numType(type1, type2)) {
        case INTEGER: return moduloInt(item1, item2, info);
        case DOUBLE:  return moduloDbl(item1, item2, info);
        case FLOAT: return moduloFlt(item1, item2, info);
        default: return moduloDec(item1, item2, info);
      }
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
   * @param info input info (can be {@code null})
   * @return result type
   * @throws QueryException query exception
   */
  public abstract Item eval(Item item1, Item item2, InputInfo info) throws QueryException;

  /**
   * Optimizes the expressions.
   * @param expr1 first expression
   * @param expr2 second expression
   * @param info input info (can be {@code null})
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
   * Checks if this is one of the specified types.
   * @param calcs types
   * @return result of check
   */
  public final boolean oneOf(final Calc... calcs) {
    for(final Calc calc : calcs) {
      if(this == calc) return true;
    }
    return false;
  }

  /**
   * Returns the numeric type with the highest precedence.
   * @param type1 first item type
   * @param type2 second item type
   * @return type
   */
  public static AtomType numType(final Type type1, final Type type2) {
    if(!type1.isNumberOrUntyped() || !type2.isNumberOrUntyped()) return ANY_ATOMIC_TYPE;
    if(type1 == DOUBLE || type2 == DOUBLE || type1.isUntyped() || type2.isUntyped()) return DOUBLE;
    if(type1 == FLOAT || type2 == FLOAT) return FLOAT;
    if(type1 == DECIMAL || type2 == DECIMAL) return DECIMAL;
    if(type1 == NUMERIC || type2 == NUMERIC) return NUMERIC;
    return INTEGER;
  }

  /**
   * Returns a type error.
   * @param info input info (can be {@code null})
   * @param type1 first type
   * @param type2 second type
   * @return query exception
   */
  final QueryException typeError(final InputInfo info, final Type type1, final Type type2) {
    return CALCTYPE_X_X_X.get(info, info(), type1, type2);
  }

  /**
   * Returns a duration type error.
   * @param info input info (can be {@code null})
   * @param item item
   * @return duration
   * @throws QueryException query exception
   */
  static Dur dur(final InputInfo info, final Item item) throws QueryException {
    final Type type = item.type;
    if(item instanceof Dur) {
      if(type == DURATION) throw NOSUBDUR_X.get(info, item);
      return (Dur) item;
    }
    throw NODUR_X_X.get(info, type, item);
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
