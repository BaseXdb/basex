package org.basex.query.func.proc;

import java.io.*;

import org.basex.io.out.*;

/**
 * Process result.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ProcResult {
  /** Process output. */
  final ArrayOutput output = new ArrayOutput();
  /** Process error. */
  final ArrayOutput error = new ArrayOutput();
  /** Options. */
  final ProcOptions options;
  /** Encoding (can be {@code null}). */
  final String encoding;
  /** Process exception (can be {@code null}). */
  IOException exception;
  /** Exit code. */
  int code;

  /**
   * Constructor.
   * @param options options
   * @param encoding encoding (can be {@code null})
   */
  ProcResult(final ProcOptions options, final String encoding) {
    this.options = options;
    this.encoding = encoding;
  }

  /**
   * Assigns an exception if none has been assigned yet.
   * @param ex exception
   */
  synchronized void exception(final IOException ex) {
    if(exception == null) exception = ex;
  }
}
