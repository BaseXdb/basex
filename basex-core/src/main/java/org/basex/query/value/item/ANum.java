package org.basex.query.value.item;

import static java.lang.Float.*;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract super class for all numeric items.
 * Useful for removing exceptions and unifying hash values.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public abstract class ANum extends Item {
  /**
   * Constructor.
   * @param t type
   */
  ANum(final Type t) {
    super(t);
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
  public abstract byte[] string();

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
  public abstract float flt();

  @Override
  public Item test(final QueryContext ctx, final InputInfo ii) throws QueryException {
    return dbl() == ctx.pos ? this : null;
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
