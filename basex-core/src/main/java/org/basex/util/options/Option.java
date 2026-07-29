package org.basex.util.options;

import org.basex.query.value.type.*;

/**
 * Single option, stored in {@link Options} instances.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param <O> option type
 */
public abstract class Option<O> {
  /** Name. */
  private final String name;
  /** Required type (can be {@code null}). */
  private final SeqType seqType;

  /**
   * Constructor without required type.
   * @param name name
   */
  Option(final String name) {
    this(name, null);
  }

  /**
   * Constructor.
   * @param name name
   * @param seqType required type (can be {@code null})
   */
  Option(final String name, final SeqType seqType) {
    this.name = name;
    this.seqType = seqType;
  }

  /**
   * Returns the name of the option.
   * @return name
   */
  public final String name() {
    return name;
  }

  /**
   * Returns the required type of values that are supplied in option maps.
   * @return type or {@code null}
   */
  public final SeqType seqType() {
    return seqType != null ? seqType : defaultType();
  }

  /**
   * Returns the required type of subclasses that always enforce one.
   * @return type or {@code null}
   */
  SeqType defaultType() {
    return null;
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
