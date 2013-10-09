package org.basex.util.options;


/**
 * Single option, stored in {@link Options} instances.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class Option {
  /** Name. */
  private final String name;

  /**
   * Constructor without default value.
   * @param n name
   */
  protected Option(final String n) {
    name = n;
  }

  /**
   * Returns the name of the option.
   * @return name
   */
  public String name() {
    return name;
  }

  /**
   * Returns the default value.
   * @return default value
   */
  public abstract Object value();
}
