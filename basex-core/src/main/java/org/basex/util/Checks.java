package org.basex.util;

/**
 * Functional interface for boolean array checks.
 *
 * @author BaseX Team 2005-19, BSD License
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
  default boolean any(Iterable<T> values) {
    for(final T value : values) if(ok(value)) return true;
    return false;
  }

  /**
   * Returns {@code true} if all value checks are successful.
   * @param values values to check
   * @return result of check
   */
  default boolean all(Iterable<T> values) {
    for(final T value : values) if(!ok(value)) return false;
    return true;
  }

  /**
   * Returns {@code true} if at least one value check is successful.
   * @param values values to check
   * @return result of check
   */
  default boolean any(T[] values) {
    for(final T value : values) if(ok(value)) return true;
    return false;
  }

  /**
   * Returns {@code true} if all value checks are successful.
   * @param values values to check
   * @return result of check
   */
  default boolean all(T[] values) {
    for(final T value : values) if(!ok(value)) return false;
    return true;
  }

  /**
   * Returns the result of a check.
   * @param value single value
   * @return result of check
   */
  boolean ok(T value);
}
