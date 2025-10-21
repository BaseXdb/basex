package org.basex.util;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

/**
 * <p>This class provides convenience operations for enums.</p>
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Enums {
  /** Enum cache. */
  private static final ConcurrentMap<Class<? extends Enum<?>>, Map<String, ? extends Enum<?>>>
    CACHE = new ConcurrentHashMap<>();

  /** Hidden constructor. */
  private Enums() { }

  /**
   * Returns the specified enum value.
   * @param <V> enum value
   * @param type enum class
   * @param value value to be found
   * @return enum or {@code null}
   */
  public static <V extends Enum<V>> V get(final Class<V> type, final String value) {
    @SuppressWarnings("unchecked")
    final Map<String, V> map = (Map<String, V>) CACHE.computeIfAbsent(type, k -> Arrays.stream(
        type.getEnumConstants()).collect(Collectors.toMap(Enum::toString, t -> t, (f, s) -> f)));
    return map.get(value);
  }

  /**
   * Checks if this is one of the specified errors.
   * @param <V> enum value
   * @param value value to be found
   * @param candidates candidates
   * @return result of check
   */
  @SafeVarargs
  public static <V extends Enum<V>> boolean oneOf(final V value, final V... candidates) {
    for(final V c : candidates) {
      if(value == c) return true;
    }
    return false;
  }

  /**
   * Helper function for converting enumeration names to strings.
   * @param en enumeration
   * @return lower-case string with '-' replaced by '-';
   */
  public static String string(final Enum<?> en) {
    return en.name().toLowerCase(Locale.ENGLISH).replace('_', '-');
  }
}
