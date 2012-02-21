package org.basex.api;

import org.basex.util.Util;

/**
 * HTTP exception. Also thrown to pass on correct status codes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class HTTPException extends Exception {
  /** Status code. */
  private final int stat;

  /**
   * Constructs an exception with the specified message and extension.
   * @param status status code
   * @param message message, or {@code null}
   * @param extension message extension
   */
  public HTTPException(final int status, final String message,
      final Object... extension) {

    super(Util.info(message, extension));
    stat = status;
  }

  /**
   * Returns the status code.
   * @return status code
   */
  public int getStatus() {
    return stat;
  }
}
