package org.basex.query;

/**
 * Function that consumes results.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 * @param <T> the type of the input to the operation
 * @param <U> the type of the second argument to the function
 */
@FunctionalInterface
public interface QueryBiConsumer<T, U> {
  /**
   * Performs this operation on the given argument.
   *
   * @param t the first input argument
   * @param u the second input argument
   * @throws QueryException query exception
   */
  void accept(T t, U u) throws QueryException;
}
