package org.basex.util;

/**
 * This class contains a single option definition.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class Option {
  /** Key. */
  public final String key;
  /** Default value. */
  public final Object value;

  /**
   * Constructor for comment options.
   * @param k key
   */
  public Option(final String k) {
    key = k;
    value = null;
  }

  /**
   * Constructor for boolean options.
   * @param k key
   * @param v value
   */
  public Option(final String k, final boolean v) {
    key = k;
    value = v;
  }

  /**
   * Constructor for string options.
   * @param k key
   * @param v value
   */
  public Option(final String k, final String v) {
    key = k;
    value = v;
  }

  /**
   * Constructor for numeric options.
   * @param k key
   * @param v value
   */
  public Option(final String k, final int v) {
    key = k;
    value = v;
  }

  /**
   * Constructor for integer array options.
   * @param k key
   * @param v value
   */
  public Option(final String k, final int[] v) {
    key = k;
    value = v;
  }

  /**
   * Constructor for string array options.
   * @param k key
   * @param v value
   */
  public Option(final String k, final String[] v) {
    key = k;
    value = v;
  }

  @Override
  public String toString() {
    return value == null ? key : key + '=' + value;
  }
}
