package org.basex.query;

/**
 * Function that supplies results.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param <T> type of results supplied by this supplier
 */
@FunctionalInterface
public interface QuerySupplier<T> {
  /**
   * Gets a result.
   *
   * @return function result
   * @throws QueryException query exception
   */
  T get() throws QueryException;
}
