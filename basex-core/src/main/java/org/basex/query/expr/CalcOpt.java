package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Optimized calculation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
interface CalcOpt {
  /**
   * Returns an optimized arithmetic calculation.
   * @param st1 first sequence type
   * @param st2 second sequence type
   * @param calc calculation operator
   * @return operator or {@code null}
   */
  static CalcOpt get(final SeqType st1, final SeqType st2, final Calc calc) {
    final AtomType type = Calc.numType(st1.type, st2.type);
    if(type.isNumber()) {
      switch(calc) {
        case ADD:
          switch(type) {
            case DOUBLE:  return (item1, item2, info) -> addDbl(item1, item2, info);
            case FLOAT:   return (item1, item2, info) -> addFlt(item1, item2, info);
            case INTEGER: return (item1, item2, info) -> addInt(item1, item2, info);
            default: break;
          }
          break;
        case SUBTRACT:
          switch(type) {
            case DOUBLE:  return (item1, item2, info) -> subtractDbl(item1, item2, info);
            case FLOAT:   return (item1, item2, info) -> subtractFlt(item1, item2, info);
            case INTEGER: return (item1, item2, info) -> subtractInt(item1, item2, info);
            default: break;
          }
          break;
        case MULTIPLY:
          switch(type) {
            case DOUBLE:  return (item1, item2, info) -> multiplyDbl(item1, item2, info);
            case FLOAT:   return (item1, item2, info) -> multiplyFlt(item1, item2, info);
            case INTEGER: return (item1, item2, info) -> multiplyInt(item1, item2, info);
            default:
          }
          break;
        case DIVIDE:
          switch(type) {
            case DOUBLE:  return (item1, item2, info) -> divideDbl(item1, item2, info);
            case FLOAT:   return (item1, item2, info) -> divideFlt(item1, item2, info);
            case INTEGER: return (item1, item2, info) -> divideDec(item1, item2, info);
            default:
          }
          break;
        case DIVIDEINT:
          switch(type) {
            case DOUBLE:  return (item1, item2, info) -> divideIntDbl(item1, item2, info);
            case FLOAT:   return (item1, item2, info) -> divideIntFlt(item1, item2, info);
            case INTEGER: return (item1, item2, info) -> divideIntInt(item1, item2, info);
            default:
          }
          break;
        case MODULO:
          switch(type) {
            case DOUBLE:  return (item1, item2, info) -> moduloDbl(item1, item2, info);
            case FLOAT:   return (item1, item2, info) -> moduloFlt(item1, item2, info);
            case INTEGER: return (item1, item2, info) -> moduloInt(item1, item2, info);
            default:
          }
          break;
        default:
      }
    }
    return null;
  }

  /**
   * Performs the calculation.
   * @param item1 first item
   * @param item2 second item
   * @param info input info (can be {@code null})
   * @return result type
   * @throws QueryException query exception
   */
  Item eval(Item item1, Item item2, InputInfo info) throws QueryException;

  /**
   * Add, two doubles.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Dbl addDbl(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    return Dbl.get(item1.dbl(info) + item2.dbl(info));
  }

  /**
   * Subtract, two doubles.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Dbl subtractDbl(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    return Dbl.get(item1.dbl(info) - item2.dbl(info));
  }

  /**
   * Multiply, two doubles.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Dbl multiplyDbl(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    return Dbl.get(item1.dbl(info) * item2.dbl(info));
  }

  /**
   * Divide, two doubles.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Dbl divideDbl(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    return Dbl.get(item1.dbl(info) / item2.dbl(info));
  }

  /**
   * Integer-divide, two doubles.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Int divideIntDbl(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    final double n1 = item1.dbl(info), n2 = item2.dbl(info), n = n1 / n2;
    if(n2 == 0) throw DIVZERO_X.get(info, item1);
    if(Double.isNaN(n) || Double.isInfinite(n)) throw INVIDIV.get(info, item1 + " idiv " + item2);
    if(n < Long.MIN_VALUE || n > Long.MAX_VALUE) throw RANGE_X.get(info, item1 + " idiv " + item2);
    return Int.get((long) n);
  }

  /**
   * Modulo, two doubles.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Dbl moduloDbl(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    return Dbl.get(item1.dbl(info) % item2.dbl(info));
  }

  /**
   * Add, two floats.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Flt addFlt(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    return Flt.get(item1.flt(info) + item2.flt(info));
  }

  /**
   * Subtract, two floats.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Flt subtractFlt(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    return Flt.get(item1.flt(info) - item2.flt(info));
  }

  /**
   * Multiply, two floats.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Flt multiplyFlt(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    return Flt.get(item1.flt(info) * item2.flt(info));
  }

  /**
   * Divide, two floats.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Flt divideFlt(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    return Flt.get(item1.flt(info) / item2.flt(info));
  }

  /**
   * Integer-divide, two floats.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Int divideIntFlt(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    final double n1 = item1.flt(info), n2 = item2.flt(info), n = n1 / n2;
    if(n2 == 0) throw DIVZERO_X.get(info, item1);
    if(Double.isNaN(n) || Double.isInfinite(n)) throw INVIDIV.get(info, item1 + " idiv " + item2);
    if(n < Long.MIN_VALUE || n > Long.MAX_VALUE) throw RANGE_X.get(info, item1 + " idiv " + item2);
    return Int.get((long) n);
  }

  /**
   * Modulo, two doubles.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Flt moduloFlt(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    return Flt.get(item1.flt(info) % item2.flt(info));
  }

  /**
   * Add, two decimals.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Dec addDec(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    return Dec.get(item1.dec(info).add(item2.dec(info)));
  }

  /**
   * Subtract, two decimals.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Dec subtractDec(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    return Dec.get(item1.dec(info).subtract(item2.dec(info)));
  }

  /**
   * Multiply, two decimals.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Dec multiplyDec(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    return Dec.get(item1.dec(info).multiply(item2.dec(info)));
  }

  /**
   * Divide, two decimals.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Dec divideDec(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    final BigDecimal dec1 = item1.dec(info), dec2 = item2.dec(info);
    if(dec2.signum() == 0) throw DIVZERO_X.get(info, item1);
    final int scale = Math.max(18, Math.max(dec1.scale(), dec2.scale()));
    return Dec.get(dec1.divide(dec2, scale, RoundingMode.HALF_EVEN));
  }

  /**
   * Integer-divide, two decimals.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Int divideIntDec(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    final BigDecimal n1 = item1.dec(info), n2 = item2.dec(info);
    if(n2.signum() == 0) throw DIVZERO_X.get(info, item1);
    final BigDecimal n = n1.divideToIntegralValue(n2);
    if(Dec.BD_MINLONG.compareTo(n) > 0 || n.compareTo(Dec.BD_MAXLONG) > 0)
      throw RANGE_X.get(info, item1 + " idiv " + item2);
    return Int.get(n.longValueExact());
  }

  /**
   * Modulo, two decimals.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Dec moduloDec(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    final BigDecimal dec1 = item1.dec(info), dec2 = item2.dec(info);
    if(dec2.signum() == 0) throw DIVZERO_X.get(info, item1);
    final BigDecimal sub = dec1.divide(dec2, 0, RoundingMode.DOWN);
    return Dec.get(dec1.subtract(sub.multiply(dec2)));
  }

  /**
   * Add, two integers.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Int addInt(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    final long itr1 = item1.itr(info), itr2 = item2.itr(info);
    if(itr2 > 0 ? itr1 > Long.MAX_VALUE - itr2 : itr1 < Long.MIN_VALUE - itr2)
      throw RANGE_X.get(info, itr1 + " + " + itr2);
    return Int.get(itr1 + itr2);
  }

  /**
   * Subtract, two integers.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Int subtractInt(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    final long itr1 = item1.itr(info), itr2 = item2.itr(info);
    if(itr2 < 0 ? itr1 > Long.MAX_VALUE + itr2 : itr1 < Long.MIN_VALUE + itr2)
      throw RANGE_X.get(info, itr1 + " - " + itr2);
    return Int.get(itr1 - itr2);
  }

  /**
   * Multiply, two integers.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Int multiplyInt(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    final long l1 = item1.itr(info), l2 = item2.itr(info);
    if(l2 > 0 ? l1 > Long.MAX_VALUE / l2 || l1 < Long.MIN_VALUE / l2
              : l2 < -1 ? l1 > Long.MIN_VALUE / l2 || l1 < Long.MAX_VALUE / l2
                        : l2 == -1 && l1 == Long.MIN_VALUE)
      throw RANGE_X.get(info, l1 + " * " + l2);
    return Int.get(l1 * l2);
  }

  /**
   * Integer-divide, two integers.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Int divideIntInt(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    final long n1 = item1.itr(info), n2 = item2.itr(info);
    if(n2 == 0) throw DIVZERO_X.get(info, item1);
    if(n1 == Integer.MIN_VALUE && n2 == -1) throw RANGE_X.get(info, item1 + " idiv " + item2);
    return Int.get(n1 / n2);
  }

  /**
   * Modulo, two integers.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  static Int moduloInt(final Item item1, final Item item2, final InputInfo info)
      throws QueryException {
    final long itr1 = item1.itr(info), itr2 = item2.itr(info);
    if(itr2 == 0) throw DIVZERO_X.get(info, item1);
    return Int.get(itr1 % itr2);
  }
}
