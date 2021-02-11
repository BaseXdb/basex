package org.basex.query;

import java.io.*;

import org.basex.util.*;

/**
 * This class indicates exceptions during the I/O operations of a query.
 * The exception contains a {@link QueryException}, which can later be unwrapped.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class QueryIOException extends IOException {
  /** Wrapped query exception. */
  private final QueryException cause;

  /**
   * Default constructor.
   * @param cause query exception
   */
  public QueryIOException(final QueryException cause) {
    super(Util.message(cause));
    this.cause = cause;
  }

  @Override
  public synchronized QueryException getCause() {
    return cause;
  }

  /**
   * Returns the query exception.
   * @param ii input info
   * @return query exception
   */
  public QueryException getCause(final InputInfo ii) {
    if(ii != null) cause.info(ii);
    return cause;
  }

  @Override
  public String getLocalizedMessage() {
    return cause.getLocalizedMessage();
  }

  @Override
  public String getMessage() {
    return cause.getMessage();
  }
}
