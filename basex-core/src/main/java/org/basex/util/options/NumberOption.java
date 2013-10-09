package org.basex.util.options;

/**
 * Integer option.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class NumberOption extends Option {
  /** Default value. */
  public final Integer value;

  /**
   * Default constructor.
   * @param n name
   * @param v value
   */
  public NumberOption(final String n, final int v) {
    super(n);
    value = v;
  }

  /**
   * Constructor without default value.
   * @param n name
   */
  public NumberOption(final String n) {
    super(n);
    value = null;
  }

  @Override
  public Integer value() {
    return value;
  }

  @Override
  public String toString() {
    return new StringBuilder(name()).append('=').append(value).toString();
  }
}
