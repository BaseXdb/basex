package org.basex.query;

/**
 * Predicate that raises query exceptions.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 * @param <T> the type of the first argument to the predicate
 * @param <U> the type of the second argument the predicate
 */
@FunctionalInterface
public interface QueryBiPredicate<T, U> {
  /**
   * Evaluates this predicate on the given arguments.
   *
   * @param t the first input argument
   * @param u the second input argument
   * @return result of check
   * @throws QueryException query exception
   */
  boolean test(T t, U u) throws QueryException;
}
