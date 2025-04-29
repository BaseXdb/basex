package org.basex.query;

/**
 * Query tracer.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public interface QueryTracer {
  /**
   * Processes tracing output.
   * @param message string to be output
   * @return {@code true} if string shall be further processed by the calling function
   */
  boolean printTrace(String message);

  /**
   * Indicates if more output should be generated.
   * @param count count of traces so far
   * @return result of check
   */
  boolean moreTraces(int count);
}
