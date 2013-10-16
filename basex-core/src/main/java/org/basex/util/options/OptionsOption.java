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
    if(value == null) return null;
    final O o = newInstance();
    try {
      o.parse(value.toString());
    } catch(final Exception ex) {
      Util.notexpected(ex);
    }
    return o;
  }

  /**
   * Returns a new options instance.
   * @return options
   */
  public O newInstance() {
    try {
      return clazz.newInstance();
    } catch(final Exception ex) {
      throw Util.notexpected(ex);
    }
  }
}
