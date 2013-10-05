package org.basex.util;

/**
 * This class contains a single option definition.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class Option {
  /** Name. */
  public final String name;
  /** Default value. */
  public final Object value;

  /**
   * Constructor for comment options.
   * @param k key
   */
  public Option(final String k) {
    name = k;
    value = null;
  }

  /**
   * Constructor for boolean options.
   * @param k key
   * @param v value
   */
  public Option(final String k, final boolean v) {
    name = k;
    value = v;
  }

  /**
   * Constructor for string options.
   * @param k key
   * @param v value
   */
  public Option(final String k, final String v) {
    name = k;
    value = v;
  }

  /**
   * Constructor for numeric options.
   * @param k key
   * @param v value
   */
  public Option(final String k, final int v) {
    name = k;
    value = v;
  }

  /**
   * Constructor for integer array options.
   * @param k key
   * @param v value
   */
  public Option(final String k, final int[] v) {
    name = k;
    value = v;
  }

  /**
   * Constructor for string array options.
   * @param k key
   * @param v value
   */
  public Option(final String k, final String[] v) {
    name = k;
    value = v;
  }

  @Override
  public String toString() {
    return value == null ? name : name + '=' + value;
  }
}
