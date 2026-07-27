package org.basex.tests.bxapi;

import org.basex.query.*;
import org.basex.util.*;

/**
 * XQuery error. Inherited from {@link RuntimeException} to provide
 * generic iterators.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQueryException extends RuntimeException {
  /**
   * Constructor.
   * @param ex exception
   */
  public XQueryException(final QueryException ex) {
    super(ex);
  }

  /**
   * Constructor.
   * @param ex exception
   */
  public XQueryException(final Exception ex) {
    super(new QueryException(ex));
  }

  /**
   * Constructor.
   * @param msg message
   */
  public XQueryException(final String msg) {
    super(new QueryException(msg));
  }

  /**
   * Constructor.
   * @param msg message
   * @param cause cause
   */
  public XQueryException(final String msg, final Throwable cause) {
    super(new QueryException(msg).cause(cause));
  }

  /**
   * Returns the causing query exception.
   * @return query exception
   */
  public QueryException getException() {
    return (QueryException) getCause();
  }

  /**
   * Returns the error code.
   * @return error code
   */
  public String getCode() {
    return Token.string(getException().qname().local());
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
