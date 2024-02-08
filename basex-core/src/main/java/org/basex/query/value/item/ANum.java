package org.basex.query.value.item;

import static java.lang.Float.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract super class for all numeric items.
 * Useful for removing exceptions and unifying hash values.
 *
 * @author BaseX Team 2005-24, BSD License
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
  public final boolean atomicEqual(final Item item, final InputInfo ii) throws QueryException {
    if(item instanceof ANum) {
      final double d1 = dbl(ii), d2 = item.dbl(ii);
      final boolean n1 = Double.isNaN(d1), n2 = Double.isNaN(d2);
      if(n1 || n2) return n1 == n2;
      if(Double.isInfinite(d1) || Double.isInfinite(d2)) return d1 == d2;
      return dec(ii).compareTo(item.dec(ii)) == 0;
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
   * Returns an ceiling value.
   * @return ceiling value
   */
  public abstract ANum ceiling();

  /**
   * Returns an floor value.
   * @return floor value
   */
  public abstract ANum floor();

  /**
   * Returns a rounded value.
   * @param scale scale
   * @param even half-to-even flag
   * @return rounded value
   */
  public abstract ANum round(int scale, boolean even);

  @Override
  public final boolean comparable(final Item item) {
    return item instanceof ANum;
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final boolean predicate)
      throws QueryException {
    return predicate ? dbl() == qc.focus.pos : bool(ii);
  }

  @Override
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {
    // predicate: E[0]  ->  E[false()]
    // EBV: if(0)  ->  if(false())
    final double d = dbl();
    return cc.simplify(this, mode == Simplify.PREDICATE && (d != itr() || d < 1) ||
        mode == Simplify.EBV && d == 0 ? Bln.FALSE : this, mode);
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
    return fractional || this instanceof Int ? this : Int.get(l);
  }

  @Override
  public final int hash(final InputInfo ii) {
    // makes sure the hashing is good for very small and very big numbers
    final long l = itr();
    final float f = flt(ii);

    // extract fractional part from a finite float
    final int frac = f == POSITIVE_INFINITY || f == NEGATIVE_INFINITY ||
        isNaN(f) ? 0 : floatToIntBits(f - l);

    int h = frac ^ (int) (l ^ l >>> 32);

    // this part ensures better distribution of bits (from java.util.HashMap)
    h ^= h >>> 20 ^ h >>> 12;
    return h ^ h >>> 7 ^ h >>> 4;
  }

  @Override
  public final void toString(final QueryString qs) {
    qs.token(string(null));
  }
}
