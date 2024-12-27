package org.basex.query;

/**
 * Predicate that raises query exceptions.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 * @param <T> the type of the input to the predicate
 */
@FunctionalInterface
public interface QueryPredicate<T> {
  /**
   * Evaluates this predicate on the given arguments.
   *
   * @param t the input argument
   * @return result of check
   * @throws QueryException query exception
   */
  boolean test(T t) throws QueryException;
}
