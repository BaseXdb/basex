package org.basex.query.func.proc;

import java.io.*;

import org.basex.util.*;

/**
 * Process result.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class ProcResult {
  /** Process output. */
  final TokenBuilder output = new TokenBuilder();
  /** Process error. */
  final TokenBuilder error = new TokenBuilder();
  /** Process exception. */
  IOException exception;
  /** Exit code. */
  int code;

  /**
   * Assigns an exception if none has been assigned yet.
   * @param ex exception
   */
  synchronized void exception(final IOException ex) {
    if(exception == null) exception = ex;
  }
}
