package org.basex.query.value.item;

import java.math.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.FnRound.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract super class for all numeric items.
 * Useful for removing exceptions and unifying hash values.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public abstract class ANum extends Item {
  /** Largest integer that has an exact double representation. */
  private static final long EXACT = 1L << 53;

  /**
   * Constructor.
   * @param type type
   */
  ANum(final Type type) {
    super(type);
  }

  /* Removing "throws QueryException" */

  @Override
  public final byte[] string(final InputInfo ii) {
    return string();
  }

  @Override
  public final double dbl(final InputInfo ii) {
    return dbl();
  }

  @Override
  public final long itr(final InputInfo ii) {
    return itr();
  }

  @Override
  public final float flt(final InputInfo ii) {
    return flt();
  }

  @Override
  public final boolean atomicEqual(final Item item) throws QueryException {
    if(this == item) return true;
    if(item instanceof ANum) {
      final double d1 = dbl(), d2 = item.dbl(null);
      final boolean n1 = Double.isNaN(d1), n2 = Double.isNaN(d2);
      if(n1 || n2) return n1 == n2;
      if(Double.isInfinite(d1) || Double.isInfinite(d2)) return d1 == d2;
      // floating-point numbers are exact double values
      if(floating() && ((ANum) item).floating()) return d1 == d2;
      return dec(null).compareTo(item.dec(null)) == 0;
    }
    return false;
  }

  /**
   * Returns a string representation of the value.
   * @return string value
   */
  public abstract byte[] string();

  /**
   * Returns a string representation of the value in JavaScript notation.
   * @return string value
   */
  public byte[] jsonString() {
    return string();
  }

  /**
   * Returns an integer (long) representation of the value.
   * @return long value
   */
  public abstract long itr();

  /**
   * Returns a double representation of the value.
   * @return double value
   */
  public abstract double dbl();

  /**
   * Returns the 32-bit integer that is equal to this number.
   * @return integer or {@link Integer#MIN_VALUE}
   */
  public int toInt() {
    final double d = dbl();
    final int i = (int) d;
    return d == i ? i : Integer.MIN_VALUE;
  }

  /**
   * Returns a float representation of the value.
   * @return float value
   */
  protected abstract float flt();

  /**
   * Returns an absolute value.
   * @return absolute value
   */
  public abstract ANum abs();

  /**
   * Returns a ceiling value.
   * @return ceiling value
   */
  public abstract ANum ceiling();

  /**
   * Returns a floor value.
   * @return floor value
   */
  public abstract ANum floor();

  /**
   * Returns a rounded value.
   * @param prec precision
   * @param mode rounding mode
   * @return rounded value
   */
  public abstract ANum round(int prec, RoundMode mode);

  @Override
  public final boolean comparable(final Item item) {
    return item instanceof ANum;
  }

  /**
   * Converts an untyped value to an integer or decimal or, if this fails, to a double.
   * @param string string
   * @param ii input info (can be {@code null})
   * @return number
   * @throws QueryException query exception
   */
  public static ANum decimal(final byte[] string, final InputInfo ii) throws QueryException {
    final long l = Token.toLong(string);
    if(l != Long.MIN_VALUE) return Itr.get(l);
    final BigDecimal bd = Dec.parse(string, ii, false);
    return bd != null ? Dec.get(bd) : Dbl.get(Dbl.parse(string, ii));
  }

  /**
   * Returns the numeric value of an item, converting untyped values to integers or decimals.
   * @param item item
   * @param ii input info (can be {@code null})
   * @return number
   * @throws QueryException query exception
   */
  public static ANum decimal(final Item item, final InputInfo ii) throws QueryException {
    return item instanceof final ANum num ? num : decimal(item.string(ii), ii);
  }

  /**
   * Checks if this is a floating-point number.
   * @return result of check
   */
  public final boolean floating() {
    return this instanceof Dbl || this instanceof Flt;
  }

  /**
   * Returns the exact integer value of this number.
   * @return integer value, or {@code null} if the number is fractional, NaN or infinite
   * @throws QueryException query exception
   */
  public final BigDecimal integer() throws QueryException {
    if(floating()) {
      final double d = dbl();
      if(!Double.isFinite(d) || d != Math.rint(d)) return null;
    }
    final BigDecimal bd = dec(null);
    return bd.scale() <= 0 || bd.stripTrailingZeros().scale() <= 0 ? bd : null;
  }

  /**
   * Compares a number with the numeric value of an item.
   * @param item value to be compared
   * @param transitive transitive comparison
   * @param ii input info
   * @return difference difference
   * @throws QueryException query exception
   */
  final int compare(final Item item, final boolean transitive, final InputInfo ii)
      throws QueryException {
    // untyped value: cast to primitive type of this number (decimal: if possible)
    final Item num2;
    if(item.type.isUntyped()) {
      final byte[] string = item.string(ii);
      if(this instanceof Dbl) {
        num2 = Dbl.get(Dbl.parse(string, ii));
      } else if(this instanceof Flt) {
        num2 = Flt.get(Flt.parse(string, ii));
      } else {
        num2 = decimal(string, ii);
      }
    } else {
      num2 = item;
    }

    if(num2 instanceof final Itr itr2) {
      if(this instanceof Itr) return Long.compare(itr(), itr2.itr());
    } else if(num2 instanceof final ANum n && n.floating()) {
      final double d = num2.dbl(ii);
      if(!Double.isFinite(d)) {
        return d == Double.NEGATIVE_INFINITY ? 1 : d == Double.POSITIVE_INFINITY ? -1 :
          transitive ? 1 : NAN_DUMMY;
      }
      // integers below the exact double range can be compared without decimal conversion
      if(this instanceof Itr) {
        final long l = itr();
        if(l >= -EXACT && l <= EXACT) {
          final double d1 = l;
          return d1 < d ? -1 : d1 > d ? 1 : 0;
        }
      }
    }
    return dec(ii).compareTo(num2.dec(ii));
  }

  @Override
  public boolean predicate(final QueryContext qc, final InputInfo ii, final long pos) {
    return dbl() == pos;
  }

  @Override
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {
    Expr expr = this;
    final double d = dbl();
    if(mode == Simplify.PREDICATE && (d != itr() || d < 1) || mode == Simplify.EBV && d == 0) {
      // predicate: E[0] → E[false()]
      // EBV: if(0) → if(false())
      expr = Bln.FALSE;
    }
    return cc.simplify(this, expr, mode);
  }

  @Override
  public final Expr optimizePos(final CmpOp op, final CompileContext cc) throws QueryException {
    final double d = dbl();
    // exact position, or 0 if the number is fractional or out of range
    final long l = Pos.position(this);
    switch(op) {
      case EQ: if(l <= 0) return Bln.FALSE; break;
      case NE: if(l <= 0) return Bln.TRUE; break;
      case LE: if(d < 1) return Bln.FALSE; break;
      case GT: if(d < 1) return Bln.TRUE; break;
      case LT: if(d < Math.nextUp(1d)) return Bln.FALSE; break;
      case GE: if(d < Math.nextUp(1d)) return Bln.TRUE; break;
    }
    // convert numbers without fractional part
    return this instanceof Itr || l <= 0 ? this : Itr.get(l);
  }

  @Override
  public final int hashCode() {
    // equal values for different numeric types must return identical hash values!
    // 0.0 and -0.0 must yield the same hash
    final double d = dbl() + 0.0;
    final int i = (int) d;
    // fast path: value fits in int
    if(d == i) return i;
    // general path: distribute bits to improve hashing
    final long bits = Double.doubleToLongBits(d);
    int h = (int) (bits ^ bits >>> 32);
    h ^= h >>> 20 ^ h >>> 12;
    return h ^ h >>> 7 ^ h >>> 4;
  }

  @Override
  public final void toString(final QueryString qs) {
    qs.token(string(null));
  }
}
