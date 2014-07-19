package org.basex.util.options;

/**
 * Option containing a boolean value.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class BooleanOption extends Option<Boolean> {
  /** Default value. */
  private final Boolean value;

  /**
   * Default constructor.
   * @param name name
   * @param value value
   */
  public BooleanOption(final String name, final boolean value) {
    super(name);
    this.value = value;
  }

  /**
   * Constructor without default value.
   * @param name name
   */
  public BooleanOption(final String name) {
    super(name);
    value = null;
  }

  @Override
  public Boolean value() {
    return value;
  }
}
