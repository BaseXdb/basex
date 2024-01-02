package org.basex.http;

import java.io.*;

/**
 * HTTP exception. Also thrown to pass on correct status codes.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class HTTPException extends IOException {
  /** Status code. */
  private final int status;

  /**
   * Constructs an exception with the specified message and extension.
   * @param description description
   * @param status status code
   */
  HTTPException(final String description, final int status) {
    super(description);
    this.status = status;
  }

  /**
   * Returns the status code.
   * @return status code
   */
  public int getStatus() {
    return status;
  }
}
