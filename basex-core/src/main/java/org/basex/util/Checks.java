package org.basex.util;

/**
 * Functional interface for boolean checks on the values of arrays and iterables.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param <T> the type of the input to the predicate
 */
@FunctionalInterface
public interface Checks<T> {
  /**
   * Returns the result of a check.
   * @param value single value
   * @return result of check
   */
  boolean ok(T value);

  /**
   * Returns {@code true} if at least one value check is successful.
   * @param <T> value type
   * @param values values to check
   * @param check check to apply
   * @return result of check
   */
  static <T> boolean any(final Iterable<T> values, final Checks<T> check) {
    for(final T value : values) if(check.ok(value)) return true;
    return false;
  }

  /**
   * Returns {@code true} if all value checks are successful.
   * @param <T> value type
   * @param values values to check
   * @param check check to apply
   * @return result of check
   */
  static <T> boolean all(final Iterable<T> values, final Checks<T> check) {
    for(final T value : values) if(!check.ok(value)) return false;
    return true;
  }

  /**
   * Returns the index of the first successful check.
   * @param <T> value type
   * @param values values to check
   * @param check check to apply
   * @return index, {@code -1} otherwise
   */
  static <T> int index(final Iterable<T> values, final Checks<T> check) {
    int i = 0;
    for(final T value : values) {
      if(check.ok(value)) return i;
      i++;
    }
    return -1;
  }

  /**
   * Returns {@code true} if at least one value check is successful.
   * @param <T> value type
   * @param values values to check
   * @param check check to apply
   * @return result of check
   */
  static <T> boolean any(final T[] values, final Checks<T> check) {
    for(final T value : values) if(check.ok(value)) return true;
    return false;
  }

  /**
   * Returns {@code true} if all value checks are successful.
   * @param <T> value type
   * @param values values to check
   * @param check check to apply
   * @return result of check
   */
  static <T> boolean all(final T[] values, final Checks<T> check) {
    for(final T value : values) if(!check.ok(value)) return false;
    return true;
  }

  /**
   * Returns the index of the first successful check.
   * @param <T> value type
   * @param values values to check
   * @param check check to apply
   * @return index, {@code -1} otherwise
   */
  static <T> int index(final T[] values, final Checks<T> check) {
    int i = 0;
    for(final T value : values) {
      if(check.ok(value)) return i;
      i++;
    }
    return -1;
  }
}
