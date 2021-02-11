package org.basex.util;

/**
 * A pair consisting of two elements.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @param <N> name
 * @param <V> value
 */
public final class Pair<N, V> {
  /** Name. */
  private final N name;
  /** Value. */
  private final V value;

  /**
   * Constructor.
   * @param name name
   * @param value value
   */
  public Pair(final N name, final V value) {
    this.name = name;
    this.value = value;
  }

  /**
   * Returns the name.
   * @return name
   */
  public N name() {
    return name;
  }

  /**
   * Returns the value.
   * @return value
   */
  public V value() {
    return value;
  }
}
