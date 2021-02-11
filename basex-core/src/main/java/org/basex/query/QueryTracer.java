package org.basex.query;

/**
 * Query tracer.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public interface QueryTracer {
  /**
   * Processes tracing output.
   * @param info string to be output
   * @return {@code true} if string shall be further processed by the calling function
   */
  boolean print(String info);
}
