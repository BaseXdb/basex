package org.basex.query.value.item;

import java.math.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpV.*;
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
      return dec(null).compareTo(item.dec(null)) == 0;
    }
    return false;
  }

  /**
   * Returns a string representation of the value.
   * @return string value
   */
  protected abstract byte[] string();

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
   * Compares a number with the numeric value of an item.
   * @param item value to be compared
   * @param transitive transitive comparison
   * @param ii input info
   * @return difference difference
   * @throws QueryException query exception
   */
  final int compare(final Item item, final boolean transitive, final InputInfo ii)
      throws QueryException {
    // if possible, compare numbers as long values
    final Item num2;
    if(item.type.isUntyped()) {
      final byte[] string = item.string(ii);
      final long l = Token.toLong(string);
      if(l != Long.MIN_VALUE) {
        num2 = Itr.get(l);
      } else {
        final BigDecimal bd = Dec.parse(string, ii, false);
        num2 = bd != null ? Dec.get(bd) : Dbl.get(Dbl.parse(string, ii));
      }
    } else {
      num2 = item;
    }

    if(num2 instanceof Itr itr2) {
      if(this instanceof Itr) return Long.compare(itr(), itr2.itr());
    } else if(num2 instanceof Dbl || num2 instanceof Flt) {
      final double d = num2.dbl(ii);
      if(!Double.isFinite(d)) {
        return d == Double.NEGATIVE_INFINITY ? 1 : d == Double.POSITIVE_INFINITY ? -1 :
          transitive ? 1 : NAN_DUMMY;
      }
    }
    return dec(ii).compareTo(num2.dec(ii));
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    return pos > 0 ? dbl() == pos : bool(ii);
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
  public final Expr optimizePos(final OpV op, final CompileContext cc) {
    final double d = dbl();
    final long l = (long) d;
    final boolean fractional = d != l;
    switch(op) {
      case EQ: if(d < 1 || fractional) return Bln.FALSE; break;
      case NE: if(d < 1 || fractional) return Bln.TRUE; break;
      case LE: if(d < 1) return Bln.FALSE; break;
      case GT: if(d < 1) return Bln.TRUE; break;
      case LT: if(d < Math.nextUp(1d)) return Bln.FALSE; break;
      case GE: if(d < Math.nextUp(1d)) return Bln.TRUE; break;
    }
    // convert numbers without fractional part
    return fractional || this instanceof Itr ? this : Itr.get(l);
  }

  @Override
  public int hashCode() {
    // equal values for different numeric types must return identical hash values!
    final long l = itr();
    final float f = flt();
    // extract fractional part from a finite float; distribute bits to improve hashing
    int h = Float.floatToRawIntBits(f - l) ^ Long.hashCode(l);
    h ^= h >>> 20 ^ h >>> 12;
    return h ^ h >>> 7 ^ h >>> 4;
  }

  @Override
  public final void toString(final QueryString qs) {
    qs.token(string(null));
  }
}
