package org.basex.tests.w3c.qt3api;

import org.basex.query.QueryException;

/**
 * XQuery error. Inherited from {@link RuntimeException} to provide
 * generic iterators.
 */
public final class XQException extends RuntimeException {
  /**
   * Constructor.
   * @param ex exception
   */
  public XQException(final QueryException ex) {
    super(ex);
  }

  /**
   * Returns the causing query exception.
   * @return query exception
   */
  public QueryException getException() {
    return (QueryException) getCause();
  }

  @Override
  public String getLocalizedMessage() {
    return getCause().getLocalizedMessage();
  }

  @Override
  public String getMessage() {
    return getCause().getMessage();
  }

  @Override
  public String toString() {
    return getCause().toString();
  }
}
