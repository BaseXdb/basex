package org.basex.util;

import java.util.*;

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
   * Constructor for options without value (used for commenting option files).
   * @param k key
   */
  public Option(final String k) {
    name = k;
    value = null;
  }

  /**
   * Constructor for option with boolean value.
   * @param k key
   * @param v value
   */
  public Option(final String k, final Boolean v) {
    name = k;
    value = v;
  }

  /**
   * Constructor for option with string value.
   * @param k key
   * @param v value
   */
  public Option(final String k, final String v) {
    name = k;
    value = v;
  }

  /**
   * Constructor for option with integer value.
   * @param k key
   * @param v value
   */
  public Option(final String k, final Integer v) {
    name = k;
    value = v;
  }

  /**
   * Constructor for option with integer array value.
   * @param k key
   * @param v value
   */
  public Option(final String k, final int[] v) {
    name = k;
    value = v;
  }

  /**
   * Constructor for option with string value value.
   * @param k key
   * @param v value
   */
  public Option(final String k, final String[] v) {
    name = k;
    value = v;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(name);
    if(value != null) {
      sb.append('=').append(
        value instanceof String[] ? Arrays.asList((String[]) value) :
        value instanceof int[] ? Arrays.asList((int[]) value) : value);
    }
    return sb.toString();
  }
}
