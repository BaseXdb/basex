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
   */
  @SuppressWarnings("unused")
  default void printTrace(final String message) {
  }

  /**
   * Indicates if more output should be generated.
   * @param count count of traces so far
   * @return result of check
   */
  @SuppressWarnings("unused")
  default boolean moreTraces(final int count) {
    return true;
  }

  /**
   * Indicates if the trace output should be cached.
   * @return result of check
   */
  default boolean cacheTrace() {
    return true;
  }
}
