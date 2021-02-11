package org.basex.util;

/**
 * Functional interface for boolean array checks.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @param <T> the type of the input to the predicate
 */
@FunctionalInterface
public interface Checks<T> {
  /**
   * Returns {@code true} if at least one value check is successful.
   * @param values values to check
   * @return result of check
   */
  default boolean any(final Iterable<T> values) {
    for(final T value : values) if(ok(value)) return true;
    return false;
  }

  /**
   * Returns {@code true} if all value checks are successful.
   * @param values values to check
   * @return result of check
   */
  default boolean all(final Iterable<T> values) {
    for(final T value : values) if(!ok(value)) return false;
    return true;
  }

  /**
   * Returns the index of the first successful check.
   * @param values values to check
   * @return index, {@code -1} otherwise
   */
  default int index(final Iterable<T> values) {
    int i = 0;
    for(final T value : values) {
      if(ok(value)) return i;
      i++;
    }
    return -1;
  }

  /**
   * Returns {@code true} if at least one value check is successful.
   * @param values values to check
   * @return result of check
   */
  default boolean any(final T[] values) {
    for(final T value : values) if(ok(value)) return true;
    return false;
  }

  /**
   * Returns {@code true} if all value checks are successful.
   * @param values values to check
   * @return result of check
   */
  default boolean all(final T[] values) {
    for(final T value : values) if(!ok(value)) return false;
    return true;
  }

  /**
   * Returns the index of the first successful check.
   * @param values values to check
   * @return index, {@code -1} otherwise
   */
  default int index(final T[] values) {
    int i = 0;
    for(final T value : values) {
      if(ok(value)) return i;
      i++;
    }
    return -1;
  }

  /**
   * Returns the result of a check.
   * @param value single value
   * @return result of check
   */
  boolean ok(T value);
}
