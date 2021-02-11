package org.basex.index.query;

import org.basex.index.*;
import org.basex.util.*;

/**
 * This class stores a string range for index access.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class StringRange implements IndexSearch {
  /** Index type. */
  private final IndexType type;
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
   * @param type index type
   * @param min minimum value
   * @param mni include minimum value
   * @param max maximum value
   * @param mxi include maximum value
   */
  public StringRange(final IndexType type, final byte[] min, final boolean mni, final byte[] max,
      final boolean mxi) {
    this.type = type;
    this.min = min;
    this.mni = mni;
    this.max = max;
    this.mxi = mxi;
  }

  @Override
  public IndexType type() {
    return type;
  }

  @Override
  public byte[] token() {
    return Token.EMPTY;
  }
}
