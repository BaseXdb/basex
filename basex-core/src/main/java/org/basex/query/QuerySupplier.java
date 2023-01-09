package org.basex.query;

/**
 * Function that supplies results.
 *
 * @author BaseX Team 2005-23, BSD License
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
