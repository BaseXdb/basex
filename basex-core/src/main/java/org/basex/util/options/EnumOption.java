package org.basex.util.options;

/**
 * Option containing an enumeration value.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 * @param <V> enumeration value
 */
public final class EnumOption<V extends Enum<V>> extends Option<V> {
  /** Class. */
  private final Class<V> clazz;
  /** Default value. */
  private final V value;

  /**
   * Constructor.
   * @param n name
   * @param v value
   */
  @SuppressWarnings("unchecked")
  public EnumOption(final String n, final V v) {
    super(n);
    value = v;
    clazz = (Class<V>) v.getClass();
  }

  /**
   * Constructor.
   * @param n name
   * @param v value
   */
  public EnumOption(final String n, final Class<V> v) {
    super(n);
    clazz = v;
    value = null;
  }

  @Override
  public V value() {
    return value;
  }

  /**
   * Returns an enumeration value for the specified string, or {@code null}.
   * @param string value
   * @return enumeration
   */
  public V get(final String string) {
    for(final V v : values()) if(v.toString().equals(string)) return v;
    return null;
  }

  /**
   * Returns all enumeration values.
   * @return enumeration
   */
  public V[] values() {
    return clazz.getEnumConstants();
  }
}
