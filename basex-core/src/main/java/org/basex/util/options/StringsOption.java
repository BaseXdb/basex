package org.basex.util.options;

import java.util.*;

/**
 * String option.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class StringsOption extends Option {
  /** Default value. */
  private final String[] value;

  /**
   * Default constructor.
   * @param n name
   * @param v value
   */
  public StringsOption(final String n, final String... v) {
    super(n);
    value = v;
  }

  @Override
  public String[] value() {
    return value;
  }

  @Override
  public String toString() {
    return new StringBuilder(name()).append(Arrays.asList(value)).toString();
  }
}
