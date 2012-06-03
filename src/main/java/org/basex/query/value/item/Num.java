package org.basex.query.value.item;

import static java.lang.Float.*;

import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract super class for all numeric items.
 * Useful for removing exceptions and unifying hash values.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
abstract class Num extends Item {
  /**
   * Constructor.
   * @param t type
   */
  Num(final Type t) {
    super(t);
  }

  /* Removing "throws QueryException" */

  @Override
  public abstract byte[] string(final InputInfo ii);

  @Override
  public abstract double dbl(InputInfo ii);

  @Override
  public abstract long itr(InputInfo ii);

  /**
   * Returns an integer (long) representation of the value.
   * @return long value
   */
  public final long itr() {
    return itr(null);
  }

  @Override
  public abstract float flt(InputInfo ii);

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
