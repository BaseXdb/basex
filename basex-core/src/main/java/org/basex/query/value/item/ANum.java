package org.basex.query.value.item;

import static java.lang.Float.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract super class for all numeric items.
 * Useful for removing exceptions and unifying hash values.
 *
 * @author BaseX Team 2005-21, BSD License
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
  public boolean sameKey(final Item item, final InputInfo ii) throws QueryException {
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
   * Returns an double representation of the value.
   * @return double value
   */
  public abstract double dbl();

  /**
   * Returns an float representation of the value.
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
  public Item test(final QueryContext qc, final InputInfo ii) {
    return dbl() == qc.focus.pos ? this : null;
  }

  @Override
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc) {
    // predicate: E[0]  ->  E[false()]
    // EBV: if(0)  ->  if(false())
    final double d = dbl();
    return cc.simplify(this, mode == Simplify.PREDICATE && (d != itr() || d < 1) ||
        mode == Simplify.EBV && d == 0 ? Bln.FALSE : this);
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
  public final void plan(final QueryString qs) {
    qs.token(string(null));
  }
}
