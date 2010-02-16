package org.basex.index;

import org.basex.data.Data.IndexType;
import org.basex.util.Token;

/**
 * This class defines access to index text tokens.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class RangeToken implements IndexToken {
  /** Index type. */
  private final IndexType type;
  /** Minimum value. */
  public double min;
  /** Maximum value. */
  public double max;

  /**
   * Constructor.
   * @param t index type
   * @param mn minimum value
   * @param mx maximum value
   */
  public RangeToken(final boolean t, final double mn, final double mx) {
    type = t ? IndexType.TXT : IndexType.ATV;
    min = mn;
    max = mx;
  }

  public IndexType type() {
    return type;
  }

  public byte[] get() {
    return Token.EMPTY;
  }
}
