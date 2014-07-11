package org.basex.tests.bxapi;

import org.basex.query.*;
import org.basex.util.*;

/**
 * XQuery error. Inherited from {@link RuntimeException} to provide
 * generic iterators.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class XQueryException extends RuntimeException {
  /**
   * Constructor.
   * @param ex exception
   */
  public XQueryException(final QueryException ex) {
    super(ex);
    Util.debug(ex);
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
