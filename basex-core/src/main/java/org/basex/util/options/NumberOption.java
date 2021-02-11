package org.basex.util.options;

/**
 * Option containing an integer value.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class NumberOption extends Option<Integer> {
  /** Default value. */
  private final Integer value;

  /**
   * Default constructor.
   * @param name name
   * @param value value
   */
  public NumberOption(final String name, final int value) {
    super(name);
    this.value = value;
  }

  /**
   * Constructor without default value.
   * @param name name
   */
  public NumberOption(final String name) {
    super(name);
    value = null;
  }

  @Override
  public Integer value() {
    return value;
  }
}
