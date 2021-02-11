package org.basex.util.options;

import org.basex.util.list.*;

/**
 * Option containing an enumeration value.
 *
 * @author BaseX Team 2005-21, BSD License
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
   * @param name name
   * @param value value
   */
  @SuppressWarnings("unchecked")
  public EnumOption(final String name, final V value) {
    super(name);
    this.value = value;
    clazz = (Class<V>) value.getClass();
  }

  /**
   * Constructor.
   * @param name name
   * @param clazz class
   */
  public EnumOption(final String name, final Class<V> clazz) {
    super(name);
    this.clazz = clazz;
    value = null;
  }

  @Override
  public V value() {
    return value;
  }

  /**
   * Returns an enum for the specified string.
   * @param string value
   * @return enum or {@code null}
   */
  public V get(final String string) {
    for(final V v : values()) {
      if(v.toString().equals(string)) return v;
    }
    return null;
  }

  /**
   * Returns all enumeration values.
   * @return enumeration
   */
  public V[] values() {
    return clazz.getEnumConstants();
  }

  /**
   * Returns all enumeration values as strings.
   * @return enumeration
   */
  public String[] strings() {
    final V[] values = values();
    final StringList sl = new StringList(values.length);
    for(final V v : values) sl.add(v.toString());
    return sl.finish();
  }
}
