package org.basex.query;

/**
 * Predicate that raises query exceptions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param <T> type of the first argument to the predicate
 * @param <U> type of the second argument the predicate
 */
@FunctionalInterface
public interface QueryBiPredicate<T, U> {
  /**
   * Evaluates this predicate on the given arguments.
   *
   * @param t first input argument
   * @param u second input argument
   * @return result of check
   * @throws QueryException query exception
   */
  boolean test(T t, U u) throws QueryException;
}
