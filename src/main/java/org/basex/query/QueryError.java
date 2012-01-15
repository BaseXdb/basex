package org.basex.query;

/**
 * An Error wrapping a {@link QueryException}, used for throwing those out of
 * methods whose interface doesn't allow it.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class QueryError extends Error {
  /**
   * Constructor.
   *
   * @param qe query exception to wrap
   */
  public QueryError(final QueryException qe) {
    super(qe);
  }

  /**
   * Getter for the wrapped exception.
   * @return wrapped query exception
   */
  public QueryException wrapped() {
    return (QueryException) getCause();
  }

  @Override
  public synchronized Throwable fillInStackTrace() {
    // do nothing for performance reasons, the stack's never used anyway
    return this;
  }
}
