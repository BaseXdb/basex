package org.basex.query;

/**
 * Function that consumes results.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param <T> type of the input to the operation
 * @param <U> type of the second argument to the function
 */
@FunctionalInterface
public interface QueryBiConsumer<T, U> {
  /**
   * Performs this operation on the given argument.
   *
   * @param t first input argument
   * @param u second input argument
   * @throws QueryException query exception
   */
  void accept(T t, U u) throws QueryException;
}
