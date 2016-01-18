package org.basex.index.query;

import org.basex.index.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * This class stores a numeric range for index access.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class NumericRange implements IndexToken {
  /** Index info. */
  private final IndexInfo ii;
  /** Minimum value. */
  public final double min;
  /** Maximum value. */
  public final double max;

  /**
   * Constructor.
   * @param ii index info
   * @param min minimum value
   * @param max maximum value
   */
  public NumericRange(final IndexInfo ii, final double min, final double max) {
    this.ii = ii;
    this.min = min;
    this.max = max;
  }

  @Override
  public IndexType type() {
    return ii.text ? IndexType.TEXT : IndexType.ATTRIBUTE;
  }

  @Override
  public byte[] get() {
    return Token.EMPTY;
  }
}
