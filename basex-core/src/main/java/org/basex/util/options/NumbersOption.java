package org.basex.util.options;

import java.util.*;

/**
 * Option containing an integer array value.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class NumbersOption extends Option<int[]> {
  /** Default value. */
  private final int[] value;

  /**
   * Default constructor.
   * @param n name
   * @param v value
   */
  public NumbersOption(final String n, final int... v) {
    super(n);
    value = v;
  }

  @Override
  public int[] value() {
    return value;
  }

  @Override
  public int[] copy() {
    return value == null ? null : value.clone();
  }

  @Override
  public String toString() {
    return name() + Arrays.asList(value);
  }
}
