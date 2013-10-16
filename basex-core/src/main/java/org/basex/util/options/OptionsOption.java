package org.basex.util.options;

import org.basex.util.*;

/**
 * String option.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 * @param <O> enumeration value
 */
public final class OptionsOption<O extends Options> extends Option {
  /** Class. */
  private final Class<O> clazz;
  /** Default value. */
  private final O value;

  /**
   * Constructor without default value.
   * @param n name
   * @param v value
   */
  @SuppressWarnings("unchecked")
  public OptionsOption(final String n, final O v) {
    super(n);
    value = v;
    clazz = (Class<O>) v.getClass();
  }

  /**
   * Constructor without default value.
   * @param n name
   * @param v value
   */
  public OptionsOption(final String n, final Class<O> v) {
    super(n);
    clazz = v;
    value = null;
  }

  @Override
  public O value() {
    return value;
  }

  @Override
  public O copy() {
    return value == null ? null : Reflect.get(Reflect.find(clazz, String.class), value.toString());
  }

  /**
   * Returns a new options instance.
   * @return options
   */
  public O newInstance() {
    return Reflect.get(clazz);
  }
}
