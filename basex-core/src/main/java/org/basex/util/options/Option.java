package org.basex.util.options;

/**
 * Single option, stored in {@link Options} instances.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @param <O> option type
 */
public abstract class Option<O> {
  /** Name. */
  private final String name;

  /**
   * Constructor without default value.
   * @param name name
   */
  Option(final String name) {
    this.name = name;
  }

  /**
   * Returns the name of the option.
   * @return name
   */
  public final String name() {
    return name;
  }

  /**
   * Returns the default value.
   * @return default value
   */
  public abstract O value();

  /**
   * Copies mutable default values. Otherwise, returns the existing instance.
   * @return default value
   */
  public O copy() {
    return value();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(name());
    final Object v = value();
    if(v != null && !v.toString().isEmpty()) sb.append('=').append(v);
    return sb.toString();
  }

  /**
   * Returns a string representation of the option with the specified argument.
   * @param arg argument
   * @return string representation
   */
  public final String arg(final String arg) {
    return "declare option output:" + name() + " '" + arg.replace("'", "''") + "';";
  }
}
