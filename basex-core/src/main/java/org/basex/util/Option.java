package org.basex.util;

import java.util.*;

/**
 * Single option, stored in {@link Options} instances.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class Option {
  /** Name. */
  public final String name;
  /** Default value. */
  public final Object value;
  /** Type. */
  public final Type type;

  /** Option type. */
  public enum Type {
    /** String.  */ STRING,
    /** Strings. */ STRINGS,
    /** Number.  */ NUMBER,
    /** Numbers. */ NUMBERS,
    /** Boolean. */ BOOLEAN,
    /** Comment. */ COMMENT
  };

  /**
   * Option without default value.
   * @param n name
   * @param t type
   */
  public Option(final String n, final Type t) {
    this(n, null, t);
  }

  /**
   * Option with string value.
   * @param n name
   * @param v value
   */
  public Option(final String n, final String v) {
    this(n, v, Type.STRING);
  }

  /**
   * Option with string value value.
   * @param n name
   * @param v value
   */
  public Option(final String n, final String[] v) {
    this(n, v, Type.STRINGS);
  }

  /**
   * Option with integer value.
   * @param n name
   * @param v value
   */
  public Option(final String n, final Integer v) {
    this(n, v, Type.NUMBER);
  }

  /**
   * Option with integer array value.
   * @param n name
   * @param v value
   */
  public Option(final String n, final int[] v) {
    this(n, v, Type.NUMBERS);
  }

  /**
   * Option with boolean value.
   * @param n name
   * @param v value
   */
  public Option(final String n, final Boolean v) {
    this(n, v, Type.BOOLEAN);
  }

  /**
   * Commenting option.
   * @param n name
   */
  public Option(final String n) {
    this(n, null, Type.COMMENT);
  }

  /**
   * Private constructor.
   * @param k key
   * @param v value
   * @param t type
   */
  private Option(final String k, final Object v, final Type t) {
    name = k;
    value = v;
    type = t;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(name);
    switch(type) {
      case COMMENT:
        break;
      case STRING:
      case BOOLEAN:
      case NUMBER:
        sb.append('=').append(value);
        break;
      case NUMBERS:
        sb.append('=').append(Arrays.asList((int[]) value));
        break;
      case STRINGS:
        sb.append('=').append(Arrays.asList((String[]) value));
        break;
    }
    return sb.toString();
  }
}
