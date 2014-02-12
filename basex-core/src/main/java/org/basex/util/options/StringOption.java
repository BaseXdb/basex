package org.basex.util.options;

/**
 * Option containing a string value.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class StringOption extends Option<String> {
  /** Default value. */
  private final String value;

  /**
   * Constructor without default value.
   * @param n name
   */
  public StringOption(final String n) {
    this(n, null);
  }

  /**
   * Default constructor.
   * @param n name
   * @param v value
   */
  public StringOption(final String n, final String v) {
    super(n);
    value = v;
  }

  @Override
  public String value() {
    return value;
  }
}
