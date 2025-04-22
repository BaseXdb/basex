package org.basex.util.options;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

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
    return get(clazz, string);
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

  /**
   * Helper function for converting enumeration names to strings.
   * @param en enumeration
   * @return lower-case string with '-' replaced by '-';
   */
  public static String string(final Enum<?> en) {
    return en.name().toLowerCase(Locale.ENGLISH).replace('_', '-');
  }

  /** Enum cache. */
  private static final ConcurrentMap<Class<? extends Enum<?>>, Map<String, ? extends Enum<?>>>
    CACHE = new ConcurrentHashMap<>();

  /**
   * Returns the specified enum value.
   * @param <T> enum type
   * @param type enum class
   * @param value value to be found
   * @return enum or {@code null}
   */
  public static <T extends Enum<T>> T get(final Class<T> type, final String value) {
    @SuppressWarnings("unchecked")
    final Map<String, T> map = (Map<String, T>) CACHE.computeIfAbsent(type, k -> Arrays.stream(
        type.getEnumConstants()).collect(Collectors.toMap(Enum::toString, t -> t, (f, s) -> f)));
    return map.get(value);
  }
}
