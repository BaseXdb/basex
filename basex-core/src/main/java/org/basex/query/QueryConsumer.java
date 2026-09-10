package org.basex.query;

/**
 * Function that consumes results.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param <T> type of the input to the operation
 */
@FunctionalInterface
public interface QueryConsumer<T> {
  /**
   * Performs this operation on the given argument.
   *
   * @param t input argument
   * @throws QueryException query exception
   */
  void accept(T t) throws QueryException;
}
