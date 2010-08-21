package org.basex.index;

import org.basex.util.Token;

/**
 * This class defines access to index text tokens.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class RangeToken implements IndexToken {
  /** Index type. */
  private final IndexType ind;
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
    ind = i ? IndexType.TEXT : IndexType.ATTV;
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
