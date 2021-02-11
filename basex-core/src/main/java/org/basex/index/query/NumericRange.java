package org.basex.index.query;

import org.basex.index.*;
import org.basex.util.*;

/**
 * This class stores a numeric range for index access.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class NumericRange implements IndexSearch {
  /** Index type. */
  private final IndexType type;
  /** Minimum value. */
  public final double min;
  /** Maximum value. */
  public final double max;

  /**
   * Constructor.
   * @param type index type
   * @param min minimum value
   * @param max maximum value
   */
  public NumericRange(final IndexType type, final double min, final double max) {
    this.type = type;
    this.min = min;
    this.max = max;
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
