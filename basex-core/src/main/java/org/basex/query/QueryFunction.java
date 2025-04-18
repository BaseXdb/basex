package org.basex.query;

/**
 * Function that raises query exceptions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface QueryFunction<T, R> {
  /**
   * Applies this function to the given argument.
   *
   * @param t the function argument
   * @return the function result
   * @throws QueryException query exception
   */
  R apply(T t) throws QueryException;
}
