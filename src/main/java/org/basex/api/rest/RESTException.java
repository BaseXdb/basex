package org.basex.api.rest;

import org.basex.util.Util;

/**
 * REST exception.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class RESTException extends Exception {
  /** Status code. */
  private final int stat;

  /**
   * Constructs an exception with the specified message and extension.
   * @param status status code
   * @param message message
   * @param extension message extension
   */
  public RESTException(final int status, final String message,
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
