package org.basex.index;

import org.basex.util.Token;

/**
 * This class stores a string range for index access.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class StringRange implements IndexToken {
  /** Index type. */
  public final IndexType type;
  /** Minimum value. */
  public final byte[] min;
  /** Include minimum value. */
  public final boolean mni;
  /** Maximum value. */
  public final byte[] max;
  /** Include maximum value. */
  public final boolean mxi;

  /**
   * Constructor.
   * @param it index type (text or attribute)
   * @param mn minimum value
   * @param in include minimum value
   * @param mx maximum value
   * @param ix include maximum value
   */
  public StringRange(final IndexType it, final byte[] mn, final boolean in,
      final byte[] mx, final boolean ix) {
    type = it;
    min = mn;
    mni = in;
    max = mx;
    mxi = ix;
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
