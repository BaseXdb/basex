package org.basex.query;

/**
 * A runtime exception wrapping a {@link QueryException}, used for throwing those out of
 * methods whose interface doesn't allow it.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class QueryRTException extends RuntimeException {
  /**
   * Constructor.
   * @param cause query exception to wrap
   */
  public QueryRTException(final QueryException cause) {
    super(cause);
  }

  @Override
  public synchronized QueryException getCause() {
    return (QueryException) super.getCause();
  }

  @Override
  public synchronized Throwable fillInStackTrace() {
    // do nothing for performance reasons, the stack's never used anyway
    return this;
  }
}
