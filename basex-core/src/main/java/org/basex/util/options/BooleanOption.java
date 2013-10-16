package org.basex.util.options;

/**
 * Option containing a boolean value.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class BooleanOption extends Option {
  /** Default value. */
  public final Boolean value;

  /**
   * Default constructor.
   * @param n name
   * @param v value
   */
  public BooleanOption(final String n, final boolean v) {
    super(n);
    value = v;
  }

  /**
   * Constructor without default value.
   * @param n name
   */
  public BooleanOption(final String n) {
    super(n);
    value = null;
  }

  @Override
  public Boolean value() {
    return value;
  }
}
