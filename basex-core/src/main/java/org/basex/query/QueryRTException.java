package org.basex.query;

/**
 * A runtime exception wrapping a {@link QueryException}, used for throwing those out of
 * methods whose interface does not allow it.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class QueryRTException extends RuntimeException {
  /**
   * Constructor.
   * @param cause query exception to wrap
   */
  public QueryRTException(final QueryException cause) {
    super(null, cause, false, false);
  }

  @Override
  public synchronized QueryException getCause() {
    return (QueryException) super.getCause();
  }
}
