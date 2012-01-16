package org.basex.index;

import org.basex.util.Token;

/**
 * This class defines access to index text tokens.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class RangeToken implements IndexToken {
  /** Index type. */
  public final IndexType ind;
  /** Minimum value. */
  public final double min;
  /** Maximum value. */
  public final double max;

  /**
   * Constructor.
   * @param i index type
   * @param mn minimum value
   * @param mx maximum value
   */
  public RangeToken(final boolean i, final double mn, final double mx) {
    ind = i ? IndexType.TEXT : IndexType.ATTRIBUTE;
    min = mn;
    max = mx;
  }

  @Override
  public IndexType type() {
    return ind;
  }

  @Override
  public byte[] get() {
    return Token.EMPTY;
  }
}
