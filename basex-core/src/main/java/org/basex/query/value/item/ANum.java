package org.basex.query.value.item;

import static java.lang.Float.*;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract super class for all numeric items.
 * Useful for removing exceptions and unifying hash values.
 *
 * @author BaseX Team 2005-14, BSD License
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
  public abstract ANum round(final int scale, final boolean even);

  @Override
  public Item test(final QueryContext qc, final InputInfo ii) throws QueryException {
    return dbl() == qc.pos ? this : null;
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
  public final String toString() {
    return Token.string(string(null));
  }
}
