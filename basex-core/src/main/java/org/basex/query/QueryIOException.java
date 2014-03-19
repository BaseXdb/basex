package org.basex.query;

import java.io.*;

import org.basex.util.*;

/**
 * This class indicates exceptions during the I/O operations of a query.
 * The exception contains a {@link QueryException}, which can later be unwrapped.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class QueryIOException extends IOException {
  /** Wrapped query exception. */
  private final QueryException exception;

  /**
   * Default constructor.
   * @param qe query exception
   */
  public QueryIOException(final QueryException qe) {
    super(Util.message(qe));
    exception = qe;
  }

  @Override
  public synchronized QueryException getCause() {
    return exception;
  }

  /**
   * Returns the query exception.
   * @param info input info
   * @return query exception
   */
  public QueryException getCause(final InputInfo info) {
    if(info != null) exception.info(info);
    return exception;
  }

  @Override
  public String getLocalizedMessage() {
    return exception.getLocalizedMessage();
  }

  @Override
  public String getMessage() {
    return exception.getMessage();
  }
}
