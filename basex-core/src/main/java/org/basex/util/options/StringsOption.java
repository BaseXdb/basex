package org.basex.util.options;

import java.util.*;

/**
 * Option containing an strings array value.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class StringsOption extends Option<String[]> {
  /** Default value. */
  private final String[] value;

  /**
   * Default constructor.
   * @param name name
   * @param value value
   */
  public StringsOption(final String name, final String... value) {
    super(name);
    this.value = value;
  }

  @Override
  public String[] value() {
    return value;
  }

  @Override
  public String[] copy() {
    return value == null ? null : value.clone();
  }

  @Override
  public String toString() {
    return name() + Arrays.asList(value);
  }
}
