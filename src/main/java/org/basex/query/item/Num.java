package org.basex.query.item;

import static org.basex.util.Token.*;
import org.basex.util.InputInfo;

/**
 * Superclass for all numeric items.
 * It's useful for removing exceptions and unifying hash values.
 *
 * @author BaseX Team 2005-11, BSD License
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
  public abstract byte[] atom(final InputInfo ii);

  @Override
  public abstract double dbl(InputInfo ii);

  @Override
  public abstract long itr(InputInfo ii);

  @Override
  public abstract float flt(InputInfo ii);

  @Override
  public final int hash(final InputInfo ii) {
    long bits = Double.doubleToLongBits(dbl(ii));
    return (int) (bits ^ (bits >>> 32));
  }

  @Override
  public final String toString() {
    return string(atom(null));
  }
}
