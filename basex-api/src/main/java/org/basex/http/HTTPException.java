package org.basex.http;

import java.io.*;

import org.basex.util.*;

/**
 * HTTP exception. Also thrown to pass on correct status codes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class HTTPException extends IOException {
  /** Status code. */
  private final int status;

  /**
   * Constructs an exception with the specified message and extension.
   * @param err error
   * @param ext message extension
   */
  HTTPException(final HTTPCode err, final Object... ext) {
    super(Util.info(err.desc, ext));
    status = err.code;
  }

  /**
   * Returns the status code.
   * @return status code
   */
  public int getStatus() {
    return status;
  }
}
