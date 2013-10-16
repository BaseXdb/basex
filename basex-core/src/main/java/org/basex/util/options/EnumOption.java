package org.basex.util.options;

/**
 * Enum option.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 * @param <V> enumeration value
 */
public final class EnumOption<V extends Enum<V>> extends Option {
  /** Default value. */
  private final V value;

  /**
   * Constructor.
   * @param n name
   * @param v value
   */
  public EnumOption(final String n, final V v) {
    super(n);
    value = v;
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
  @SuppressWarnings("unchecked")
  public V[] values() {
    return (V[]) value.getClass().getEnumConstants();
  }
}
