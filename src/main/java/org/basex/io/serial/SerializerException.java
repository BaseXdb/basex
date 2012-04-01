package org.basex.io.serial;

import java.io.IOException;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;

/**
 * This class indicates exceptions during the serialization of a query.
 * This exception contains a {@link QueryException}, which can later be
 * unwrapped.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class SerializerException extends IOException {
  /** Wrapped query exception. */
  private final QueryException exception;

  /**
   * Default constructor.
   * @param qe query exception
   */
  public SerializerException(final QueryException qe) {
    super(qe.getMessage());
    exception = qe;
  }

  @Override
  public QueryException getCause() {
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
