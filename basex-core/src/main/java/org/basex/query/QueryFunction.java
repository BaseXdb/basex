package org.basex.query;

/**
 * Function that raises query exceptions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param <T> type of the input to the function
 * @param <R> type of the result of the function
 */
@FunctionalInterface
public interface QueryFunction<T, R> {
  /**
   * Applies this function to the given argument.
   *
   * @param t function argument
   * @return function result
   * @throws QueryException query exception
   */
  R apply(T t) throws QueryException;
}
