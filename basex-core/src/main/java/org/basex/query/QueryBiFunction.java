package org.basex.query;

/**
 * Function that raises query exceptions.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface QueryBiFunction<T, U, R> {
  /**
   * Applies this function to the given argument.
   *
   * @param t the first function argument
   * @param u the second function argument
   * @return the function result
   * @throws QueryException query exception
   */
  R apply(T t, U u) throws QueryException;
}
