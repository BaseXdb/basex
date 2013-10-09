package org.basex.util.options;

/**
 * String option.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class StringOption extends Option {
  /** Default value. */
  private final String value;

  /**
   * Default constructor.
   * @param n name
   * @param v value
   */
  public StringOption(final String n, final String v) {
    super(n);
    value = v;
  }

  /**
   * Constructor without default value.
   * @param n name
   */
  public StringOption(final String n) {
    super(n);
    value = null;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public String toString() {
    return new StringBuilder(name()).append('=').append(value).toString();
  }
}
