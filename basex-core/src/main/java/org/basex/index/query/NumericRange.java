package org.basex.index.query;

import org.basex.index.*;
import org.basex.util.*;

/**
 * This class stores a numeric range for index access.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class NumericRange implements IndexToken {
  /** Index type. */
  private final boolean text;
  /** Minimum value. */
  public final double min;
  /** Maximum value. */
  public final double max;

  /**
   * Constructor.
   * @param text text/attribute index
   * @param min minimum value
   * @param max maximum value
   */
  public NumericRange(final boolean text, final double min, final double max) {
    this.text = text;
    this.min = min;
    this.max = max;
  }

  @Override
  public IndexType type() {
    return text ? IndexType.TEXT : IndexType.ATTRIBUTE;
  }

  @Override
  public byte[] get() {
    return Token.EMPTY;
  }
}
