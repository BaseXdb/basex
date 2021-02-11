package org.basex.util.options;

/**
 * Option containing a string value.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class StringOption extends Option<String> {
  /** Default value. */
  private final String value;

  /**
   * Constructor without default value.
   * @param name name
   */
  public StringOption(final String name) {
    this(name, null);
  }

  /**
   * Default constructor.
   * @param name name
   * @param value value
   */
  public StringOption(final String name, final String value) {
    super(name);
    this.value = value;
  }

  @Override
  public String value() {
    return value;
  }
}
