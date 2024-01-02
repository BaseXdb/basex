package org.basex.query;

/**
 * Function that consumes results.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 * @param <T> the type of the input to the operation
 */
@FunctionalInterface
public interface QueryConsumer<T> {
  /**
   * Performs this operation on the given argument.
   *
   * @param t the input argument
   * @throws QueryException query exception
   */
  void accept(T t) throws QueryException;
}
