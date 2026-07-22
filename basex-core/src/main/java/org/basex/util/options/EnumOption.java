package org.basex.util.options;

import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Option containing an enumeration value.
 *
 * @author BaseX Team, BSD License
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
  public EnumOption(final String name, final V value) {
    this(name, value, null);
  }

  /**
   * Constructor with required type. Options are stored as enum values, but the value that is
   * supplied in an option map may be of any other type.
   * @param name name
   * @param value value
   * @param seqType required type (can be {@code null})
   */
  @SuppressWarnings("unchecked")
  public EnumOption(final String name, final V value, final SeqType seqType) {
    super(name, seqType);
    this.value = value;
    clazz = (Class<V>) value.getClass();
  }

  /**
   * Constructor.
   * @param name name
   * @param clazz class
   */
  public EnumOption(final String name, final Class<V> clazz) {
    this(name, clazz, null);
  }

  /**
   * Constructor with required type.
   * @param name name
   * @param clazz class
   * @param seqType required type (can be {@code null})
   */
  public EnumOption(final String name, final Class<V> clazz, final SeqType seqType) {
    super(name, seqType);
    this.clazz = clazz;
    value = null;
  }

  @Override
  public V value() {
    return value;
  }

  @Override
  SeqType defaultType() {
    return Types.STRING_O;
  }

  /**
   * Returns an enum for the specified string.
   * @param string value
   * @return enum or {@code null}
   */
  public V get(final String string) {
    return Enums.get(clazz, string);
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
