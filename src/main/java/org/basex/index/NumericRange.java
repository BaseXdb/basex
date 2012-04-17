package org.basex.index;

import org.basex.util.Token;

/**
 * This class stores a numeric range for index access.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class NumericRange implements IndexToken {
  /** Index type. */
  public final IndexType type;
  /** Minimum value. */
  public final double min;
  /** Maximum value. */
  public final double max;

  /**
   * Constructor.
   * @param it index type
   * @param mn minimum value
   * @param mx maximum value
   */
  public NumericRange(final IndexType it, final double mn, final double mx) {
    type = it;
    min = mn;
    max = mx;
  }

  @Override
  public IndexType type() {
    return type;
  }

  @Override
  public byte[] get() {
    return Token.EMPTY;
  }
}
