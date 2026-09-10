package org.basex.query;

/**
 * Function that raises query exceptions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param <T> type of the first argument to the function
 * @param <U> type of the second argument to the function
 * @param <R> type of the result of the function
 */
@FunctionalInterface
public interface QueryBiFunction<T, U, R> {
  /**
   * Applies this function to the given argument.
   *
   * @param t first function argument
   * @param u second function argument
   * @return function result
   * @throws QueryException query exception
   */
  R apply(T t, U u) throws QueryException;
}
