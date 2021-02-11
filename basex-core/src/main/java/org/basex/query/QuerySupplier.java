package org.basex.query;

/**
 * Function that supplied results.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @param <T> the type of results supplied by this supplier
 */
@FunctionalInterface
public interface QuerySupplier<T> {
  /**
   * Gets a result.
   *
   * @return the function result
   * @throws QueryException query exception
   */
  T get() throws QueryException;
}
