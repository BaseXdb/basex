package org.basex.query;

/**
 * Predicate that raises query exceptions.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 * @param <T> the type of the input to the function
 */
@FunctionalInterface
public interface QueryPredicate<T> {
  /**
   * Applies this function to the given argument.
   *
   * @param t the function argument
   * @return the function result
   * @throws QueryException query exception
   */
  boolean test(T t) throws QueryException;
}
